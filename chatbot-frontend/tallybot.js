const bot = BotManager.getCurrentBot();

function makeTimestamp() {
  // 현재 UTC 시간에 9시간(한국 시간) 추가
  const now = new Date();
  const koreaTime = new Date(now.getTime() + (9 * 60 * 60 * 1000));
  
  // YYYY-MM-DD HH:mm:ss 형식으로 포맷팅
  const year = koreaTime.getUTCFullYear();
  const month = (koreaTime.getUTCMonth() + 1 < 10) ? '0' + (koreaTime.getUTCMonth() + 1) : '' + (koreaTime.getUTCMonth() + 1);
  const day = (koreaTime.getUTCDate() < 10) ? '0' + koreaTime.getUTCDate() : '' + koreaTime.getUTCDate();
  const hour = (koreaTime.getUTCHours() < 10) ? '0' + koreaTime.getUTCHours() : '' + koreaTime.getUTCHours();
  const minute = (koreaTime.getUTCMinutes() < 10) ? '0' + koreaTime.getUTCMinutes() : '' + koreaTime.getUTCMinutes();
  const second = (koreaTime.getUTCSeconds() < 10) ? '0' + koreaTime.getUTCSeconds() : '' + koreaTime.getUTCSeconds();
  
  return year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;
}

// 끝에 슬래시 없어야 함!
const domain = "http://tally-bot-web-backend-alb-243058276.ap-northeast-2.elb.amazonaws.com";

// 연결 테스트 함수
function testConnection() {
  try {
    const res = org.jsoup.Jsoup.connect(domain + "/test")
      .header("Accept", "application/json")
      .method(org.jsoup.Connection.Method.GET)
      .ignoreContentType(true)
      .timeout(10000)
      .execute();
    
    return "연결 성공! 상태코드: " + res.statusCode() + ", 응답: " + res.body();
  } catch (e) {
    return "연결 실패: " + e.toString();
  }
}

/**
* json을 받아 api path로 전송하고 JSON을 받아와 객체로 변환해 돌려준다.
**/
function sendJson(apiPath, json) {
  try {
    const res = org.jsoup.Jsoup.connect(domain + apiPath)
      .header("Content-Type", "application/json")
      .header("Accept", "application/json")
      .requestBody(json)
      .method(org.jsoup.Connection.Method.POST)
      .ignoreContentType(true)
      .timeout(60000)
      .execute();
      
    return {
      "statusCode": res.statusCode(),
      "body": JSON.parse(res.body())
    };
  } catch (e) {
    // 에러 정보 반환 (getClass() 제거)
    return {
      "statusCode": -1,
      "error": e.toString()
    };
  }
}

function userCreateAndSelect(msg) {
  let user = {
    "groupId": parseInt(msg.channelId), // channelId 사용
    "groupName": msg.room, // room이 채팅방 이름
    "member": msg.author.name
  }
  
  let res = sendJson('/api/group/create', JSON.stringify(user));
  
  if(res === undefined) {
    msg.reply('서버 응답이 없습니다.');
    return null;
  }
  
  if(res.statusCode === -1) {
    msg.reply('연결 오류: ' + res.error);
    return null;
  }
  
  if(res.statusCode !== 200) {
    if(res.body && res.body.error) {
      msg.reply('서버 오류: ' + res.body.error);
    } else {
      msg.reply('서버 오류가 발생했습니다.');
    }
    return null;
  }
  
  return res.body;
}

function findMemberId(group, name) {
  let j;
  for(let i = 0; i < group.members.length; i++) {
    if(group.members[i].nickname === name) {
      j = group.members[i].memberId;
      break;
    }
  }
  return j;
}

function maxDigitsBefore(str, unitIdx) {
  let i;
  for(i = unitIdx - 1; i >= 0; i--) {
    if(str[i] > '9' || str[i] < '0') break;
  }
  if(i === unitIdx - 1) return undefined;
  return parseInt(str.substring(i + 1, unitIdx));
}

function formatTwoDigit(i) {
  if (i >= 100) return undefined;
  else if (i < 10)
    return "0" + i;
  else
    return "" + i;
}

/**
 * 메시지 처리 함수
 */
function onMessage(msg) {
  if (msg.content.split(" ")[0] === ';정산') return;

  if (msg.content.indexOf('정산') !== -1) {
    let month = maxDigitsBefore(msg.content, msg.content.indexOf('월'));
    let day = maxDigitsBefore(msg.content, msg.content.indexOf('일'));
  
    let month2 = maxDigitsBefore(msg.content, msg.content.lastIndexOf('월'));
    let day2 = maxDigitsBefore(msg.content, msg.content.lastIndexOf('일'));
    
    let flag = month === undefined
      || day === undefined
      || month2 === undefined
      || day2 === undefined;
      
    if(!flag) {
    
    const now = new Date();

    let year = now.getFullYear();

    let year2 = now.getFullYear();  

    let hour = 0;
    let hour2 = 23;

    msg.command = '정산';

    let date = formatTwoDigit(year % 100) + formatTwoDigit(month) + formatTwoDigit(day) + formatTwoDigit(hour);
    let date2 = formatTwoDigit(year2 % 100) + formatTwoDigit(month2) + formatTwoDigit(day2) + formatTwoDigit(hour2);
    msg.args = [date, date2]; // 배열로 변경

    onCommand(msg);
    return;
  }
  }
  
  // 그룹 생성/조회
  let group = userCreateAndSelect(msg);
  
  // group이 null인 경우 처리
  if(group === null) {
    return; // 이미 에러 메시지가 전송됨
  }
  
  // 멤버 ID 찾기
  let memberId = findMemberId(group, msg.author.name);
  
  if(memberId === undefined) {
    msg.reply('멤버 정보를 찾을 수 없습니다.');
    return;
  }
  
  var message = {
    "groupId": parseInt(group.groupId),
    "timestamp": makeTimestamp(),
    "memberId": parseInt(memberId),
    "message": msg.content
  };
  
  var array = [message];
  
  let res = sendJson('/api/chat/upload', JSON.stringify(array));
  
  if(res === undefined) {
    msg.reply('채팅 업로드에 실패했습니다.');
    return;
  }
  
  if(res.statusCode === -1) {
    msg.reply('네트워크 오류가 발생했습니다.');
    return;
  }
  
  if(res.statusCode !== 200) {
    msg.reply('채팅 저장에 실패했습니다.');
    return;
  }
}

bot.addListener(Event.MESSAGE, onMessage);

/**
 * 명령어 처리 함수
 */
function onCommand(msg) {
  if(msg.command === '정산') {
    if(msg.args.length !== 2) {
      msg.reply('정산 인자가 잘못되었습니다.');
      return;
    }
    
    // 정규식 수정 (문자열 리터럴에서 슬래시 제거)
    let strs0 = msg.args[0].match(/^\d{8}$/);
    let strs1 = msg.args[1].match(/^\d{8}$/);
    
    if(strs0 === null || strs1 === null) {
      msg.reply('정산 인자가 잘못되었습니다. (날짜 형식: YYMMDDSS)');
      return;
    }
    
    let group = userCreateAndSelect(msg);
    if(group === null) {
      return;
    }
    
    let memberId = findMemberId(group, msg.author.name);
    
    // 날짜 파싱 수정
    strs0 = msg.args[0].match(/\d{2}/g);
    strs1 = msg.args[1].match(/\d{2}/g);
    
    let req = {
      "groupId": parseInt(group.groupId),
      "startTime": '20' + strs0[0] + '-' + strs0[1] + '-' + strs0[2]
        + ' ' + strs0[3] + ':00:00',
      "endTime": '20' + strs1[0] + '-' + strs1[1] + '-' + strs1[2]
        + ' ' + strs1[3] + ':59:59'
    }
    
    let res = sendJson('/api/calculate/start', JSON.stringify(req));
  
    if(res === undefined || res.statusCode !== 200) {
      msg.reply('정산 시작 중 오류가 발생했습니다.');
      return;
    } else {
      msg.reply('정산을 수행하고 있습니다. 잠시만 기다려 주십시오.');
    }
    
    let calculateId = res.body.calculateId;
    let now = Date.now();
    let maxWaitTime = 60000; // 최대 60초 대기
    
    // 정산 결과 폴링
    while (true) {
      try {
        let conn = org.jsoup.Jsoup.connect(domain + '/api/calculate/' + calculateId + '/brief-result')
          .ignoreContentType(true)
          .header('Accept', 'application/json')
          .header('Content-Type', 'application/json')
          .method(org.jsoup.Connection.Method.GET)
          .timeout(10000);
          
        let res2 = conn.execute(); 
        
        if(res2.statusCode() === 202) {
          let now2 = Date.now();
          if(now2 - now >= 10000) {
            msg.reply('정산을 수행하고 있습니다. 잠시만 기다려 주십시오.');
            now = now2;
          }
          
          // 최대 대기 시간 초과 체크
          if(now2 - (now - 10000) > maxWaitTime) {
            msg.reply('정산 처리 시간이 초과되었습니다. 나중에 다시 시도해주세요.');
            break;
          }
          
          // 잠시 대기
          java.lang.Thread.sleep(1000);
          
        } else if(res2.statusCode() === 200) {
          msg.reply('정산이 완료되었습니다.');
          let body = JSON.parse(res2.body());
          
          const map = new Map();
          
          for(let i = 0; i < group.members.length; i++) {
            map.set(group.members[i].memberId, group.members[i].nickname);
          }
          
          let str = '정산 결과\n';
          for(let i = 0; i < body.transfers.length; i++) {
            str += map.get(body.transfers[i].payerId) + ' -> '
              + map.get(body.transfers[i].payeeId) + ': '
              + body.transfers[i].amount + '원\n';
          }
          
          msg.reply(str  
            + '이 정산의 세부 내용을 보려면? ' + body.calculateUrl
            + '\n채팅방 전체 정산 기록을 보려면? ' + body.groupUrl);
          
          break; // 성공적으로 완료
          
        } else {
          msg.reply('정산 중 오류가 발생했습니다. 관리자에게 문의하여 주십시오.');
          break;
        }
      } catch (e) {
        msg.reply('정산 조회 중 네트워크 오류가 발생했습니다: ' + e.toString());
        break;
      }
    }
  }
}

bot.setCommandPrefix(";");
bot.addListener(Event.COMMAND, onCommand);

// Android Activity 이벤트 처리
function onCreate(savedInstanceState, activity) {
  var textView = new android.widget.TextView(activity);
  textView.setText("Tally Bot is running!");
  textView.setTextColor(android.graphics.Color.DKGRAY);
  activity.setContentView(textView);
}

function onStart(activity) {}
function onResume(activity) {}
function onPause(activity) {}
function onStop(activity) {}
function onRestart(activity) {}
function onDestroy(activity) {}
function onBackPressed(activity) {}

bot.addListener(Event.Activity.CREATE, onCreate);
bot.addListener(Event.Activity.START, onStart);
bot.addListener(Event.Activity.RESUME, onResume);
bot.addListener(Event.Activity.PAUSE, onPause);
bot.addListener(Event.Activity.STOP, onStop);
bot.addListener(Event.Activity.RESTART, onRestart);
bot.addListener(Event.Activity.DESTROY, onDestroy);
bot.addListener(Event.Activity.BACK_PRESSED, onBackPressed);
