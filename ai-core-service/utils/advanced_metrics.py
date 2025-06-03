#!/usr/bin/env python3
"""
고급 평가 메트릭 유틸리티
의미론적 분석, 문맥 이해도, 엣지 케이스 검출 등을 수행합니다.
"""

import json
import re
from typing import List, Dict, Any, Set, Tuple
from collections import Counter, defaultdict
from dataclasses import dataclass


@dataclass
class AdvancedMetricResult:
    """고급 메트릭 결과"""
    score: float
    details: Dict[str, Any]
    insights: List[str]
    recommendations: List[str]


class AdvancedSettlementMetrics:
    """고급 정산 평가 메트릭"""
    
    def __init__(self):
        # 한국어 금액 패턴 제거 - 직접 비교 방식 사용
        # self.korean_currency_patterns = [
        #     r'(\d+)원', r'(\d+)만원', r'(\d+)천원',
        #     r'(\d{1,3}(?:,\d{3})*)원'
        # ]
        self.common_places = {
            '카페', '커피숍', '스타벅스', '이디야', '투썸',
            '식당', '음식점', '맛집', '치킨집', '피자집',
            '편의점', 'GS25', 'CU', '세븐일레븐',
            '마트', '이마트', '롯데마트', '홈플러스',
            '주유소', 'SK', 'GS칼텍스', 'S-OIL'
        }
        
    def evaluate_semantic_understanding(
        self, 
        conversation: List[Dict], 
        actual_items: List[Dict], 
        expected_items: List[Dict]
    ) -> AdvancedMetricResult:
        """의미론적 이해도 평가"""
        
        # 대화에서 언급된 실제 내용 추출
        conversation_content = self._extract_conversation_content(conversation)
        
        # 의미론적 매칭 분석
        semantic_matches = self._analyze_semantic_matches(
            conversation_content, actual_items, expected_items
        )
        
        # 문맥 이해도 평가
        context_understanding = self._evaluate_context_understanding(
            conversation_content, actual_items
        )
        
        # 암시적 정보 추출 평가
        implicit_extraction = self._evaluate_implicit_extraction(
            conversation_content, actual_items, expected_items
        )
        
        # 종합 점수 계산
        total_score = (
            semantic_matches["score"] * 0.4 +
            context_understanding["score"] * 0.35 +
            implicit_extraction["score"] * 0.25
        )
        
        insights = []
        recommendations = []
        
        if semantic_matches["score"] < 0.7:
            insights.append("의미론적 매칭이 부족함")
            recommendations.append("대화 내용과 추출 결과의 의미적 연관성 개선 필요")
        
        if context_understanding["score"] < 0.7:
            insights.append("문맥 이해도가 낮음")
            recommendations.append("대화의 전체적인 맥락 파악 능력 향상 필요")
        
        return AdvancedMetricResult(
            score=total_score,
            details={
                "semantic_matches": semantic_matches,
                "context_understanding": context_understanding,
                "implicit_extraction": implicit_extraction
            },
            insights=insights,
            recommendations=recommendations
        )
    
    def evaluate_edge_case_handling(
        self, 
        conversation: List[Dict], 
        actual_items: List[Dict]
    ) -> AdvancedMetricResult:
        """엣지 케이스 처리 능력 평가"""
        
        edge_cases = {
            "ambiguous_amounts": self._detect_ambiguous_amounts(conversation, actual_items),
            "unclear_participants": self._detect_unclear_participants(conversation, actual_items),
            "complex_splitting": self._detect_complex_splitting(conversation, actual_items),
            "temporal_references": self._detect_temporal_references(conversation, actual_items),
            "conditional_payments": self._detect_conditional_payments(conversation, actual_items)
        }
        
        # 각 엣지 케이스별 처리 점수
        edge_scores = []
        for case_name, case_result in edge_cases.items():
            edge_scores.append(case_result["handling_score"])
        
        avg_score = sum(edge_scores) / len(edge_scores) if edge_scores else 1.0
        
        insights = []
        recommendations = []
        
        for case_name, case_result in edge_cases.items():
            if case_result["detected"] and case_result["handling_score"] < 0.7:
                insights.append(f"{case_name} 처리 미흡")
                recommendations.append(f"{case_name} 상황에 대한 처리 로직 개선 필요")
        
        return AdvancedMetricResult(
            score=avg_score,
            details=edge_cases,
            insights=insights,
            recommendations=recommendations
        )
    
    def evaluate_data_quality(self, actual_items: List[Dict]) -> AdvancedMetricResult:
        """데이터 품질 평가"""
        
        quality_metrics = {
            "format_consistency": self._check_format_consistency(actual_items),
            "value_reasonableness": self._check_value_reasonableness(actual_items),
            "relationship_validity": self._check_relationship_validity(actual_items),
            "completeness_distribution": self._analyze_completeness_distribution(actual_items)
        }
        
        # 가중 평균으로 종합 점수 계산
        weights = {"format_consistency": 0.3, "value_reasonableness": 0.3, 
                  "relationship_validity": 0.25, "completeness_distribution": 0.15}
        
        total_score = sum(
            quality_metrics[metric]["score"] * weight 
            for metric, weight in weights.items()
        )
        
        insights = []
        recommendations = []
        
        for metric_name, metric_result in quality_metrics.items():
            if metric_result["score"] < 0.8:
                insights.extend(metric_result.get("issues", []))
                recommendations.extend(metric_result.get("recommendations", []))
        
        return AdvancedMetricResult(
            score=total_score,
            details=quality_metrics,
            insights=insights,
            recommendations=recommendations
        )
    
    def _extract_conversation_content(self, conversation: List[Dict]) -> Dict[str, Any]:
        """대화에서 핵심 내용 추출"""
        content = {
            "mentioned_places": set(),
            "mentioned_items": set(),
            "mentioned_amounts": [],
            "mentioned_people": set(),
            "payment_keywords": [],
            "splitting_keywords": []
        }
        
        payment_patterns = ['결제', '계산', '돈', '비용', '가격', '얼마', '지불']
        splitting_patterns = ['나눠', '분할', '각자', '반반', '똑같이', '비율']
        
        for msg in conversation:
            if msg.get('speaker') == 'system':
                continue
                
            text = msg.get('message_content', '').lower()
            
            # 장소 추출
            for place in self.common_places:
                if place in text:
                    content["mentioned_places"].add(place)
            
            # 결제 관련 키워드
            for keyword in payment_patterns:
                if keyword in text:
                    content["payment_keywords"].append(keyword)
            
            # 분할 관련 키워드
            for keyword in splitting_patterns:
                if keyword in text:
                    content["splitting_keywords"].append(keyword)
        
        return content
    
    def _analyze_semantic_matches(
        self, 
        conversation_content: Dict, 
        actual_items: List[Dict], 
        expected_items: List[Dict]
    ) -> Dict[str, Any]:
        """의미론적 매칭 분석"""
        
        # 실제 추출된 금액과 기대 금액 직접 비교 (대화 파싱 대신)
        actual_amounts = sorted([item.get('amount', 0) for item in actual_items])
        expected_amounts = sorted([item.get('amount', 0) for item in expected_items]) if expected_items else []
        
        # 금액 정확도 계산
        if expected_amounts:
            # 완전 일치 확인
            if actual_amounts == expected_amounts:
                amount_score = 1.0
                amount_overlap = len(actual_amounts)
            else:
                # 부분 일치 계산
                actual_set = set(actual_amounts)
                expected_set = set(expected_amounts)
                overlap = len(actual_set & expected_set)
                amount_score = overlap / len(expected_set)
                amount_overlap = overlap
        else:
            amount_score = 1.0 if not actual_amounts else 0.0
            amount_overlap = 0
        
        # 장소 매칭
        mentioned_places = conversation_content["mentioned_places"]
        extracted_places = set(str(item.get('place', '')).lower() for item in actual_items)
        
        place_matches = 0
        for mentioned in mentioned_places:
            if any(mentioned in extracted for extracted in extracted_places):
                place_matches += 1
        
        place_score = place_matches / len(mentioned_places) if mentioned_places else 1.0
        
        # 종합 점수 (금액 비중을 높임)
        overall_score = (amount_score * 0.8 + place_score * 0.2)
        
        return {
            "score": overall_score,
            "amount_score": amount_score,
            "place_score": place_score,
            "mentioned_amounts": [],  # 더 이상 대화에서 파싱하지 않음
            "extracted_amounts": actual_amounts,
            "amount_overlap": amount_overlap
        }
    
    def _evaluate_context_understanding(
        self, 
        conversation_content: Dict, 
        actual_items: List[Dict]
    ) -> Dict[str, Any]:
        """문맥 이해도 평가"""
        
        # 결제 맥락 이해
        payment_context_score = 1.0
        if conversation_content["payment_keywords"]:
            # 결제 관련 언급이 있으면 실제로 금액이 추출되었는지 확인
            has_amounts = any(item.get('amount', 0) > 0 for item in actual_items)
            payment_context_score = 1.0 if has_amounts else 0.5
        
        # 분할 맥락 이해
        splitting_context_score = 1.0
        if conversation_content["splitting_keywords"]:
            # 분할 언급이 있으면 참여자가 여러 명인지 확인
            multi_participant_items = sum(
                1 for item in actual_items 
                if len(item.get('participants', [])) > 1
            )
            splitting_context_score = min(1.0, multi_participant_items / len(actual_items)) if actual_items else 0.0
        
        overall_score = (payment_context_score + splitting_context_score) / 2
        
        return {
            "score": overall_score,
            "payment_context_score": payment_context_score,
            "splitting_context_score": splitting_context_score
        }
    
    def _evaluate_implicit_extraction(
        self, 
        conversation_content: Dict, 
        actual_items: List[Dict], 
        expected_items: List[Dict]
    ) -> Dict[str, Any]:
        """암시적 정보 추출 평가"""
        
        # 명시적으로 언급되지 않았지만 추론 가능한 정보들
        implicit_score = 1.0
        
        # 예상 결과와 비교하여 암시적 정보 추출 정확도 측정
        if expected_items:
            # 예상 결과에는 있지만 대화에서 명시적으로 언급되지 않은 정보
            expected_amounts = set(item.get('amount', 0) for item in expected_items)
            mentioned_amounts = set(conversation_content["mentioned_amounts"])
            
            implicit_amounts = expected_amounts - mentioned_amounts
            extracted_implicit = set(item.get('amount', 0) for item in actual_items) & implicit_amounts
            
            if implicit_amounts:
                implicit_score = len(extracted_implicit) / len(implicit_amounts)
        
        return {
            "score": implicit_score,
            "implicit_extraction_accuracy": implicit_score
        }
    
    def _detect_ambiguous_amounts(self, conversation: List[Dict], actual_items: List[Dict]) -> Dict[str, Any]:
        """모호한 금액 표현 검출"""
        ambiguous_patterns = ['대충', '약', '정도', '쯤', '어느정도']
        
        detected = False
        for msg in conversation:
            text = msg.get('message_content', '').lower()
            if any(pattern in text for pattern in ambiguous_patterns):
                detected = True
                break
        
        # 모호한 표현이 있을 때 적절히 처리했는지 평가
        handling_score = 1.0
        if detected:
            # 실제 추출된 금액이 있는지 확인
            has_amounts = any(item.get('amount', 0) > 0 for item in actual_items)
            handling_score = 0.8 if has_amounts else 0.3
        
        return {
            "detected": detected,
            "handling_score": handling_score,
            "description": "모호한 금액 표현 처리"
        }
    
    def _detect_unclear_participants(self, conversation: List[Dict], actual_items: List[Dict]) -> Dict[str, Any]:
        """불분명한 참여자 검출"""
        unclear_patterns = ['누군가', '어떤 사람', '그 사람', '모르겠는데']
        
        detected = False
        for msg in conversation:
            text = msg.get('message_content', '').lower()
            if any(pattern in text for pattern in unclear_patterns):
                detected = True
                break
        
        handling_score = 1.0
        if detected:
            # 참여자가 적절히 식별되었는지 확인
            clear_participants = sum(
                1 for item in actual_items 
                if item.get('participants') and len(item['participants']) > 0
            )
            handling_score = clear_participants / len(actual_items) if actual_items else 0.0
        
        return {
            "detected": detected,
            "handling_score": handling_score,
            "description": "불분명한 참여자 처리"
        }
    
    def _detect_complex_splitting(self, conversation: List[Dict], actual_items: List[Dict]) -> Dict[str, Any]:
        """복잡한 분할 패턴 검출"""
        complex_patterns = ['비율', '퍼센트', '%', '더 많이', '적게', '다르게']
        
        detected = False
        for msg in conversation:
            text = msg.get('message_content', '').lower()
            if any(pattern in text for pattern in complex_patterns):
                detected = True
                break
        
        handling_score = 1.0
        if detected:
            # 복잡한 분할이 적절히 처리되었는지 확인
            complex_ratios = sum(
                1 for item in actual_items 
                if item.get('ratios') and len(set(item['ratios'].values())) > 1
            )
            handling_score = complex_ratios / len(actual_items) if actual_items else 0.0
        
        return {
            "detected": detected,
            "handling_score": handling_score,
            "description": "복잡한 분할 패턴 처리"
        }
    
    def _detect_temporal_references(self, conversation: List[Dict], actual_items: List[Dict]) -> Dict[str, Any]:
        """시간적 참조 검출"""
        temporal_patterns = ['어제', '오늘', '내일', '지난번', '다음에', '나중에']
        
        detected = False
        for msg in conversation:
            text = msg.get('message_content', '').lower()
            if any(pattern in text for pattern in temporal_patterns):
                detected = True
                break
        
        # 시간적 참조가 있을 때 적절한 처리 여부 (현재는 기본 점수)
        handling_score = 0.8 if detected else 1.0
        
        return {
            "detected": detected,
            "handling_score": handling_score,
            "description": "시간적 참조 처리"
        }
    
    def _detect_conditional_payments(self, conversation: List[Dict], actual_items: List[Dict]) -> Dict[str, Any]:
        """조건부 결제 검출"""
        conditional_patterns = ['만약', '~면', '~라면', '경우에', '조건']
        
        detected = False
        for msg in conversation:
            text = msg.get('message_content', '').lower()
            if any(pattern in text for pattern in conditional_patterns):
                detected = True
                break
        
        # 조건부 결제 처리 (현재는 기본 점수)
        handling_score = 0.7 if detected else 1.0
        
        return {
            "detected": detected,
            "handling_score": handling_score,
            "description": "조건부 결제 처리"
        }
    
    def _check_format_consistency(self, actual_items: List[Dict]) -> Dict[str, Any]:
        """형식 일관성 검사"""
        if not actual_items:
            return {"score": 1.0, "issues": [], "recommendations": []}
        
        issues = []
        recommendations = []
        
        # 필드 타입 일관성
        amount_types = set(type(item.get('amount', 0)) for item in actual_items)
        if len(amount_types) > 1:
            issues.append("금액 필드 타입 불일치")
            recommendations.append("모든 금액을 동일한 타입(숫자)으로 통일")
        
        # 참여자 형식 일관성
        participant_formats = set()
        for item in actual_items:
            participants = item.get('participants', [])
            if isinstance(participants, list):
                participant_formats.add('list')
            elif isinstance(participants, str):
                participant_formats.add('string')
        
        if len(participant_formats) > 1:
            issues.append("참여자 필드 형식 불일치")
            recommendations.append("참여자 정보를 리스트 형태로 통일")
        
        score = max(0.0, 1.0 - len(issues) * 0.2)
        
        return {
            "score": score,
            "issues": issues,
            "recommendations": recommendations
        }
    
    def _check_value_reasonableness(self, actual_items: List[Dict]) -> Dict[str, Any]:
        """값 합리성 검사"""
        if not actual_items:
            return {"score": 1.0, "issues": [], "recommendations": []}
        
        issues = []
        recommendations = []
        
        # 금액 합리성
        amounts = [item.get('amount', 0) for item in actual_items if item.get('amount', 0) > 0]
        if amounts:
            avg_amount = sum(amounts) / len(amounts)
            for i, amount in enumerate(amounts):
                if amount > avg_amount * 10:  # 평균의 10배 이상
                    issues.append(f"항목 {i}: 비정상적으로 높은 금액 ({amount:,}원)")
                elif amount < 100:  # 100원 미만
                    issues.append(f"항목 {i}: 비정상적으로 낮은 금액 ({amount}원)")
        
        # 참여자 수 합리성
        for i, item in enumerate(actual_items):
            participants = item.get('participants', [])
            if len(participants) > 20:  # 20명 초과
                issues.append(f"항목 {i}: 비정상적으로 많은 참여자 ({len(participants)}명)")
        
        score = max(0.0, 1.0 - len(issues) * 0.1)
        
        if issues:
            recommendations.append("비정상적인 값들을 재검토하여 정확성 확인")
        
        return {
            "score": score,
            "issues": issues,
            "recommendations": recommendations
        }
    
    def _check_relationship_validity(self, actual_items: List[Dict]) -> Dict[str, Any]:
        """관계 유효성 검사"""
        if not actual_items:
            return {"score": 1.0, "issues": [], "recommendations": []}
        
        issues = []
        recommendations = []
        
        for i, item in enumerate(actual_items):
            # 결제자가 참여자에 포함되어 있는지 확인
            payer = item.get('payer', '')
            participants = item.get('participants', [])
            
            if payer and participants and payer not in participants:
                issues.append(f"항목 {i}: 결제자가 참여자 목록에 없음")
            
            # 비율 키와 참여자 일치 확인 (합계는 중요하지 않음)
            ratios = item.get('ratios', {})
            if ratios and participants:
                ratio_keys = set(str(k) for k in ratios.keys())
                participant_set = set(str(p) for p in participants)
                
                if ratio_keys != participant_set:
                    issues.append(f"항목 {i}: 비율 키와 참여자 목록 불일치")
        
        score = max(0.0, 1.0 - len(issues) * 0.15)
        
        if issues:
            recommendations.append("결제자와 참여자 관계, 비율 정보 일관성 확인")
        
        return {
            "score": score,
            "issues": issues,
            "recommendations": recommendations
        }
    
    def _analyze_completeness_distribution(self, actual_items: List[Dict]) -> Dict[str, Any]:
        """완성도 분포 분석"""
        if not actual_items:
            return {"score": 0.0, "issues": ["추출된 항목 없음"], "recommendations": []}
        
        field_completeness = defaultdict(int)
        total_items = len(actual_items)
        
        for item in actual_items:
            for field in ['item', 'amount', 'payer', 'participants', 'place', 'hint_type']:
                if field in item and item[field] is not None:
                    if field == 'participants' and isinstance(item[field], list) and len(item[field]) > 0:
                        field_completeness[field] += 1
                    elif field != 'participants' and str(item[field]).strip():
                        field_completeness[field] += 1
        
        # 완성도 분포 계산
        completeness_rates = {
            field: count / total_items 
            for field, count in field_completeness.items()
        }
        
        # 필수 필드 완성도
        required_fields = ['item', 'amount', 'payer', 'participants']
        required_completeness = [
            completeness_rates.get(field, 0) 
            for field in required_fields
        ]
        
        avg_completeness = sum(required_completeness) / len(required_completeness)
        
        issues = []
        recommendations = []
        
        for field in required_fields:
            rate = completeness_rates.get(field, 0)
            if rate < 0.8:
                issues.append(f"{field} 필드 완성도 낮음 ({rate:.1%})")
                recommendations.append(f"{field} 필드 추출 정확도 개선 필요")
        
        return {
            "score": avg_completeness,
            "completeness_rates": completeness_rates,
            "issues": issues,
            "recommendations": recommendations
        }


def evaluate_advanced_metrics(
    conversation: List[Dict], 
    actual_items: List[Dict], 
    expected_items: List[Dict] = None
) -> Dict[str, Any]:
    """고급 메트릭 종합 평가 함수"""
    
    metrics = AdvancedSettlementMetrics()
    
    results = {}
    
    # 의미론적 이해도 평가
    if expected_items:
        semantic_result = metrics.evaluate_semantic_understanding(
            conversation, actual_items, expected_items
        )
        results["semantic_understanding"] = {
            "score": semantic_result.score,
            "details": semantic_result.details,
            "insights": semantic_result.insights,
            "recommendations": semantic_result.recommendations
        }
    
    # 엣지 케이스 처리 평가
    edge_case_result = metrics.evaluate_edge_case_handling(conversation, actual_items)
    results["edge_case_handling"] = {
        "score": edge_case_result.score,
        "details": edge_case_result.details,
        "insights": edge_case_result.insights,
        "recommendations": edge_case_result.recommendations
    }
    
    # 데이터 품질 평가
    quality_result = metrics.evaluate_data_quality(actual_items)
    results["data_quality"] = {
        "score": quality_result.score,
        "details": quality_result.details,
        "insights": quality_result.insights,
        "recommendations": quality_result.recommendations
    }
    
    # 종합 점수 계산
    scores = [result["score"] for result in results.values()]
    overall_score = sum(scores) / len(scores) if scores else 0.0
    
    # 전체 인사이트 및 권장사항 수집
    all_insights = []
    all_recommendations = []
    
    for result in results.values():
        all_insights.extend(result["insights"])
        all_recommendations.extend(result["recommendations"])
    
    return {
        "overall_score": overall_score,
        "detailed_results": results,
        "summary": {
            "total_score": f"{overall_score:.1%}",
            "evaluation_count": len(results),
            "insights": all_insights,
            "recommendations": all_recommendations
        }
    } 