#!/usr/bin/env python3
"""
정산 결과 평가 유틸리티
상세한 정산 정확도, 유사성, 할루시네이션 검출 등을 수행합니다.
"""

import json
import re
from typing import List, Dict, Any, Tuple
from difflib import SequenceMatcher
from dataclasses import dataclass


@dataclass
class EvaluationResult:
    """평가 결과 데이터 클래스"""
    score: float
    details: Dict[str, Any]
    grade: str
    issues: List[str]


class SettlementEvaluator:
    """정산 결과 종합 평가기"""
    
    def __init__(self):
        self.similarity_threshold = 0.7
        self.amount_tolerance = 0.05  # 5% 오차 허용
        
    def evaluate_comprehensive(
        self, 
        actual_items: List[Dict[str, Any]], 
        expected_items: List[Dict[str, Any]]
    ) -> EvaluationResult:
        """종합적인 정산 평가 수행"""
        
        evaluation_details = {
            "item_count": self._evaluate_item_count(actual_items, expected_items),
            "place_item_similarity": self._evaluate_place_item_similarity(actual_items, expected_items),
            "amount_accuracy": self._evaluate_amount_accuracy(actual_items, expected_items),
            "participant_accuracy": self._evaluate_participant_accuracy(actual_items, expected_items),
            "hallucination_detection": self._detect_hallucinations(actual_items, expected_items),
            "data_completeness": self._evaluate_data_completeness(actual_items),
            "consistency_check": self._check_internal_consistency(actual_items)
        }
        
        # 종합 점수 계산 (가중 평균)
        weights = {
            "item_count": 0.15,
            "place_item_similarity": 0.05,
            "amount_accuracy": 0.36,
            "participant_accuracy": 0.26,
            "hallucination_detection": 0.10,
            "data_completeness": 0.05,
            "consistency_check": 0.03
        }
        
        total_score = sum(
            evaluation_details[metric]["score"] * weight 
            for metric, weight in weights.items()
        )
        
        # 등급 계산
        grade = self._calculate_grade(total_score)
        
        # 주요 이슈 수집
        issues = []
        for metric_name, metric_result in evaluation_details.items():
            if metric_result["score"] < 0.7:
                issues.extend(metric_result.get("issues", []))
        
        return EvaluationResult(
            score=total_score,
            details=evaluation_details,
            grade=grade,
            issues=issues
        )
    
    def _evaluate_item_count(self, actual: List, expected: List) -> Dict[str, Any]:
        """항목 수 정확도 평가"""
        actual_count = len(actual)
        expected_count = len(expected)
        
        if expected_count == 0:
            score = 1.0 if actual_count == 0 else 0.0
        else:
            # 정확히 일치하면 1.0, 차이가 클수록 점수 감소
            diff_ratio = abs(actual_count - expected_count) / expected_count
            score = max(0.0, 1.0 - diff_ratio)
        
        issues = []
        if actual_count != expected_count:
            issues.append(f"항목 수 불일치: 실제 {actual_count}개, 예상 {expected_count}개")
        
        return {
            "score": score,
            "actual_count": actual_count,
            "expected_count": expected_count,
            "difference": actual_count - expected_count,
            "issues": issues
        }
    
    def _evaluate_place_item_similarity(self, actual: List, expected: List) -> Dict[str, Any]:
        """장소와 항목명 유사성 평가"""
        if not expected:
            return {"score": 1.0, "matches": [], "issues": []}
        
        matches = []
        total_similarity = 0.0
        unmatched_actual = []
        
        for actual_item in actual:
            best_match = None
            best_similarity = 0.0
            
            actual_place = str(actual_item.get('place', '')).strip()
            actual_item_name = str(actual_item.get('item', '')).strip()
            
            for expected_item in expected:
                expected_place = str(expected_item.get('place', '')).strip()
                expected_item_name = str(expected_item.get('item', '')).strip()
                
                # 장소 유사성
                place_sim = self._calculate_text_similarity(actual_place, expected_place)
                
                # 항목명 유사성
                item_sim = self._calculate_text_similarity(actual_item_name, expected_item_name)
                
                # 종합 유사성 (항목명에 더 높은 가중치)
                combined_sim = (place_sim * 0.3 + item_sim * 0.7)
                
                if combined_sim > best_similarity:
                    best_similarity = combined_sim
                    best_match = {
                        "actual": {"place": actual_place, "item": actual_item_name},
                        "expected": {"place": expected_place, "item": expected_item_name},
                        "place_similarity": place_sim,
                        "item_similarity": item_sim,
                        "combined_similarity": combined_sim
                    }
            
            if best_match and best_similarity >= self.similarity_threshold:
                matches.append(best_match)
                total_similarity += best_similarity
            else:
                unmatched_actual.append({
                    "place": actual_place,
                    "item": actual_item_name,
                    "best_similarity": best_similarity
                })
        
        # 평균 유사성 계산
        avg_similarity = total_similarity / len(actual) if actual else 0.0
        
        issues = []
        if unmatched_actual:
            issues.append(f"유사성이 낮은 항목 {len(unmatched_actual)}개 발견")
        
        return {
            "score": avg_similarity,
            "matches": matches,
            "unmatched_items": unmatched_actual,
            "average_similarity": avg_similarity,
            "issues": issues
        }
    
    def _evaluate_amount_accuracy(self, actual: List, expected: List) -> Dict[str, Any]:
        """금액 정확도 평가"""
        if not expected:
            return {"score": 1.0, "matches": [], "issues": []}
        
        amount_matches = []
        total_accuracy = 0.0
        
        # 총 금액 비교
        actual_total = sum(item.get('amount', 0) for item in actual)
        expected_total = sum(item.get('amount', 0) for item in expected)
        
        if expected_total > 0:
            total_accuracy = 1.0 - min(1.0, abs(actual_total - expected_total) / expected_total)
        else:
            total_accuracy = 1.0 if actual_total == 0 else 0.0
        
        # 개별 항목 금액 매칭
        for actual_item in actual:
            actual_amount = actual_item.get('amount', 0)
            best_match = None
            best_accuracy = 0.0
            
            for expected_item in expected:
                expected_amount = expected_item.get('amount', 0)
                
                if expected_amount > 0:
                    accuracy = 1.0 - min(1.0, abs(actual_amount - expected_amount) / expected_amount)
                else:
                    accuracy = 1.0 if actual_amount == 0 else 0.0
                
                if accuracy > best_accuracy:
                    best_accuracy = accuracy
                    best_match = {
                        "actual_amount": actual_amount,
                        "expected_amount": expected_amount,
                        "accuracy": accuracy,
                        "difference": actual_amount - expected_amount
                    }
            
            if best_match:
                amount_matches.append(best_match)
        
        issues = []
        if total_accuracy < 0.95:
            issues.append(f"총 금액 오차: 실제 {actual_total:,}원, 예상 {expected_total:,}원")
        
        return {
            "score": total_accuracy,
            "total_accuracy": total_accuracy,
            "actual_total": actual_total,
            "expected_total": expected_total,
            "amount_matches": amount_matches,
            "issues": issues
        }
    
    def _evaluate_participant_accuracy(self, actual: List, expected: List) -> Dict[str, Any]:
        """참여자 정확도 평가"""
        if not expected:
            return {"score": 1.0, "issues": []}
        
        # 결제자 정확도
        actual_payers = set(str(item.get('payer', '')) for item in actual)
        expected_payers = set(str(item.get('payer', '')) for item in expected)
        
        payer_accuracy = len(actual_payers & expected_payers) / len(expected_payers) if expected_payers else 1.0
        
        # 참여자 수 정확도
        participant_accuracies = []
        for actual_item in actual:
            actual_participants = actual_item.get('participants', [])
            actual_count = len(actual_participants) if actual_participants else 0
            
            # 가장 유사한 예상 항목 찾기
            best_expected_count = 0
            for expected_item in expected:
                expected_participants = expected_item.get('participants', [])
                expected_count = len(expected_participants) if expected_participants else 0
                if abs(actual_count - expected_count) < abs(actual_count - best_expected_count):
                    best_expected_count = expected_count
            
            if best_expected_count > 0:
                accuracy = 1.0 - abs(actual_count - best_expected_count) / best_expected_count
            else:
                accuracy = 1.0 if actual_count == 0 else 0.0
            
            participant_accuracies.append(accuracy)
        
        avg_participant_accuracy = sum(participant_accuracies) / len(participant_accuracies) if participant_accuracies else 1.0
        
        # 종합 점수
        overall_score = (payer_accuracy * 0.6 + avg_participant_accuracy * 0.4)
        
        issues = []
        if payer_accuracy < 0.8:
            issues.append(f"결제자 식별 정확도 낮음: {payer_accuracy:.1%}")
        if avg_participant_accuracy < 0.8:
            issues.append(f"참여자 수 정확도 낮음: {avg_participant_accuracy:.1%}")
        
        return {
            "score": overall_score,
            "payer_accuracy": payer_accuracy,
            "participant_accuracy": avg_participant_accuracy,
            "actual_payers": list(actual_payers),
            "expected_payers": list(expected_payers),
            "issues": issues
        }
    
    def _detect_hallucinations(self, actual: List, expected: List) -> Dict[str, Any]:
        """할루시네이션 (환각) 검출"""
        if not expected:
            return {"score": 1.0, "hallucinations": [], "issues": []}
        
        hallucinations = []
        
        # 예상 데이터에서 나타나는 모든 값들 수집
        expected_places = set()
        expected_items = set()
        expected_amounts = set()
        expected_payers = set()
        
        for item in expected:
            if item.get('place'):
                expected_places.add(str(item['place']).lower().strip())
            if item.get('item'):
                expected_items.add(str(item['item']).lower().strip())
            if item.get('amount'):
                expected_amounts.add(item['amount'])
            if item.get('payer'):
                expected_payers.add(str(item['payer']))
        
        # 실제 결과에서 예상과 전혀 다른 값들 찾기
        for i, actual_item in enumerate(actual):
            item_hallucinations = []
            
            # 장소 할루시네이션 검사
            actual_place = str(actual_item.get('place', '')).lower().strip()
            if actual_place and not any(self._calculate_text_similarity(actual_place, exp_place) > 0.5 for exp_place in expected_places):
                item_hallucinations.append(f"예상치 못한 장소: {actual_place}")
            
            # 항목 할루시네이션 검사
            actual_item_name = str(actual_item.get('item', '')).lower().strip()
            if actual_item_name and not any(self._calculate_text_similarity(actual_item_name, exp_item) > 0.5 for exp_item in expected_items):
                item_hallucinations.append(f"예상치 못한 항목: {actual_item_name}")
            
            # 금액 할루시네이션 검사 (±20% 범위 벗어나는 경우)
            actual_amount = actual_item.get('amount', 0)
            if actual_amount > 0:
                amount_reasonable = any(
                    abs(actual_amount - exp_amount) / exp_amount <= 0.2 
                    for exp_amount in expected_amounts if exp_amount > 0
                )
                if not amount_reasonable:
                    item_hallucinations.append(f"예상치 못한 금액: {actual_amount:,}원")
            
            # 결제자 할루시네이션 검사
            actual_payer = str(actual_item.get('payer', ''))
            if actual_payer and actual_payer not in expected_payers:
                item_hallucinations.append(f"예상치 못한 결제자: {actual_payer}")
            
            if item_hallucinations:
                hallucinations.append({
                    "item_index": i,
                    "item": actual_item,
                    "hallucinations": item_hallucinations
                })
        
        # 할루시네이션 점수 (할루시네이션이 적을수록 높은 점수)
        hallucination_ratio = len(hallucinations) / len(actual) if actual else 0.0
        score = max(0.0, 1.0 - hallucination_ratio)
        
        issues = []
        if hallucinations:
            issues.append(f"할루시네이션 {len(hallucinations)}개 항목에서 발견")
        
        return {
            "score": score,
            "hallucinations": hallucinations,
            "hallucination_count": len(hallucinations),
            "hallucination_ratio": hallucination_ratio,
            "issues": issues
        }
    
    def _evaluate_data_completeness(self, actual: List) -> Dict[str, Any]:
        """데이터 완성도 평가"""
        if not actual:
            return {"score": 0.0, "issues": ["추출된 항목이 없음"]}
        
        required_fields = ['item', 'amount', 'payer', 'participants']
        optional_fields = ['place', 'hint_type']
        
        completeness_scores = []
        missing_data = []
        
        for i, item in enumerate(actual):
            field_scores = []
            item_missing = []
            
            # 필수 필드 검사
            for field in required_fields:
                if field in item and item[field] is not None:
                    if field == 'participants' and isinstance(item[field], list) and len(item[field]) > 0:
                        field_scores.append(1.0)
                    elif field != 'participants' and str(item[field]).strip():
                        field_scores.append(1.0)
                    else:
                        field_scores.append(0.0)
                        item_missing.append(field)
                else:
                    field_scores.append(0.0)
                    item_missing.append(field)
            
            # 선택 필드 검사 (보너스 점수)
            for field in optional_fields:
                if field in item and item[field] is not None and str(item[field]).strip():
                    field_scores.append(0.5)  # 보너스 점수
            
            item_completeness = sum(field_scores) / len(required_fields)
            completeness_scores.append(item_completeness)
            
            if item_missing:
                missing_data.append({
                    "item_index": i,
                    "missing_fields": item_missing
                })
        
        avg_completeness = sum(completeness_scores) / len(completeness_scores)
        
        issues = []
        if missing_data:
            issues.append(f"데이터 누락: {len(missing_data)}개 항목에서 필드 누락")
        
        return {
            "score": avg_completeness,
            "average_completeness": avg_completeness,
            "missing_data": missing_data,
            "issues": issues
        }
    
    def _check_internal_consistency(self, actual: List) -> Dict[str, Any]:
        """내부 일관성 검사"""
        if not actual:
            return {"score": 1.0, "issues": []}
        
        consistency_issues = []
        
        # 중복 항목 검사
        seen_items = set()
        duplicates = []
        
        for i, item in enumerate(actual):
            item_key = (
                str(item.get('item', '')).strip().lower(),
                item.get('amount', 0),
                str(item.get('payer', '')).strip()
            )
            
            if item_key in seen_items:
                duplicates.append(f"중복 항목 발견: {item.get('item', '')} ({item.get('amount', 0):,}원)")
            else:
                seen_items.add(item_key)
        
        # 비율 합계 검사
        ratio_issues = []
        for i, item in enumerate(actual):
            ratios = item.get('ratios', {})
            if ratios:
                total_ratio = sum(ratios.values())
                if abs(total_ratio - len(item.get('participants', []))) > 0.1:
                    ratio_issues.append(f"항목 {i}: 비율 합계 불일치")
        
        # 일관성 점수 계산
        total_issues = len(duplicates) + len(ratio_issues)
        consistency_score = max(0.0, 1.0 - (total_issues / len(actual)))
        
        issues = duplicates + ratio_issues
        
        return {
            "score": consistency_score,
            "duplicates": duplicates,
            "ratio_issues": ratio_issues,
            "issues": issues
        }
    
    def _calculate_text_similarity(self, text1: str, text2: str) -> float:
        """텍스트 유사성 계산"""
        if not text1 or not text2:
            return 0.0
        
        # 기본 문자열 유사성
        basic_similarity = SequenceMatcher(None, text1.lower(), text2.lower()).ratio()
        
        # 키워드 기반 유사성 (공통 단어 비율)
        words1 = set(re.findall(r'\w+', text1.lower()))
        words2 = set(re.findall(r'\w+', text2.lower()))
        
        if words1 or words2:
            keyword_similarity = len(words1 & words2) / len(words1 | words2)
        else:
            keyword_similarity = 0.0
        
        # 가중 평균 (기본 유사성 70%, 키워드 유사성 30%)
        return basic_similarity * 0.7 + keyword_similarity * 0.3
    
    def _calculate_grade(self, score: float) -> str:
        """점수를 등급으로 변환"""
        if score >= 0.95:
            return "A+"
        elif score >= 0.90:
            return "A"
        elif score >= 0.85:
            return "B+"
        elif score >= 0.80:
            return "B"
        elif score >= 0.75:
            return "C+"
        elif score >= 0.70:
            return "C"
        elif score >= 0.60:
            return "D"
        else:
            return "F"


def evaluate_settlement_results(actual_items: List[Dict], expected_items: List[Dict]) -> Dict[str, Any]:
    """정산 결과 평가 메인 함수"""
    evaluator = SettlementEvaluator()
    result = evaluator.evaluate_comprehensive(actual_items, expected_items)
    
    return {
        "overall_score": result.score,
        "grade": result.grade,
        "detailed_metrics": result.details,
        "issues": result.issues,
        "summary": {
            "total_score": f"{result.score:.1%}",
            "performance_grade": result.grade,
            "major_issues": len(result.issues),
            "evaluation_passed": result.score >= 0.7
        }
    } 