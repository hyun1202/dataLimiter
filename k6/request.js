// K6 Rate Limiting 부하 테스트 스크립트
import { sleep } from 'k6';
import http from 'k6/http';

const scenario = __ENV.SCENARIO || 'default';
export let options = {};

// 환경변수에 따라 옵션을 다르게 함
if (scenario === 'random') {
    // 10초동안 2초 간격으로 요청
    options = {
        scenarios: {
            random: {
                executor: 'constant-arrival-rate',
                rate: 2,
                duration: '10s',
                preAllocatedVUs: 5,
            }
        }
    };
} else if (scenario === 'duplicate') {
    // 처리율 제한 테스트
    // 10초 동안 5초 간격으로 7번씩 요청
    options = {
        scenarios: {
            duplicate: {
                executor: 'per-vu-iterations',
                vus: 1,
                iterations: 2,
            }
        }
    };
} else {
    // 부하 테스트
    options = {
        scenarios: {
            default: {
                executor: 'constant-vus',
                vus: 20,            // 20명이
                duration: '5s',     // 5초 동안 계속
            }
        }
    };
}

// 랜덤 값 생성 함수들
function getRandomFilename() {
    // const randomNum = Math.floor(Math.random() * 10000) + 1;
    const randomNum = Math.floor(Math.random() * 100) + 1; // 1,2,3,4 중 하나
    return `index.html${randomNum}`;
}

function getMessage() {
    return 'undefindValue';
}

// 메인 테스트 함수
export default function () {
    if (scenario === "duplicate") {
        duplicateRequestTest();
        return;
    }
    randomTest();
}

// 테스트 완료 후 요약 출력
export function handleSummary(data) {
    const successCount = data.metrics.http_reqs ? data.metrics.http_reqs.values.count : 0;
    const failedCount = data.metrics.http_req_failed ? data.metrics.http_req_failed.values.passes : 0;

    console.log('\n=== 테스트 결과 요약 ===');
    console.log(`총 요청 수: ${successCount}`);
    console.log(`성공 요청: ${successCount - failedCount}`);
    console.log(`실패 요청: ${failedCount}`);
    console.log(`평균 응답시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);

    return {
        'summary.json': JSON.stringify(data, null, 2),
    };
}

// 한국 시간(KST) 포맷 함수 - 확실한 방법
function getKSTTime() {
    const now = new Date();
    // UTC 시간을 밀리초로 구한 후 9시간(32400000ms) 추가
    const kstMillis = now.getTime() + (9 * 60 * 60 * 1000);
    const kstDate = new Date(kstMillis);

    const hours = String(kstDate.getUTCHours()).padStart(2, '0');
    const minutes = String(kstDate.getUTCMinutes()).padStart(2, '0');
    const seconds = String(kstDate.getUTCSeconds()).padStart(2, '0');
    const milliseconds = String(kstDate.getUTCMilliseconds()).padStart(3, '0');

    return `${hours}:${minutes}:${seconds}.${milliseconds}`;
}

// === 다른 테스트 시나리오들 ===

// k6 run ./k6/request.js
// 1. 랜덤 테스트
export function randomTest() {
    const filename = getRandomFilename();
    const message = getMessage();

    const url = `http://localhost:8080/api/error?filename=${filename}&message=${message}`;

    const response = http.get(url, {
        headers: {
            'Content-Type': 'application/json',
        },
    });

    const endTime = getKSTTime();

    // 응답 상태별 로그
    if (response.status === 200) {
        console.log(`[${endTime}] ✅ [${filename}] 요청 성공`);
    } else if (response.status === 429) {
        console.log(`[${endTime}] 🚫 [${filename}] Rate Limited`);
    } else {
        console.log(`[${endTime}] ❌ [${filename}] 응답: ${response.status}`);
    }
}

// k6 run -e SCENARIO=duplicate ./k6/request.js
// 2. 완전히 동일한 요청 반복 (중복 제한 확인)
export function duplicateRequestTest() {
    const filename = 'duplicate.html';
    const message = 'sameError';

    for (let i = 0; i < 7; i++) { // 5개 제한이므로 7개 요청
        const url = `http://localhost:8080/api/error?filename=${filename}&message=${message}`;
        const response = http.get(url);

        console.log(`[${i+1}/7] 동일 요청: ${response.status}`);
        if (response.status === 429) {
            console.log(`🚫 ${i+1}번째에서 차단됨!`);
            break;
        }
    }

    // 5초 이후 요청이 되는지 확인
    sleep(5);
}