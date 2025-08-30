// K6 Rate Limiting 부하 테스트 스크립트

import http from 'k6/http';

// 테스트 설정
export let options = {
    vus: 20,        // 20명의 가상 사용자
    duration: '5s', // 5초 동안 실행

    // 또는 정확히 100번 요청을 원한다면:
    // iterations: 100,
    // duration: '5s',
};

// 랜덤 값 생성 함수들
function getRandomFilename() {
    const randomNum = Math.floor(Math.random() * 10000) + 1; // 1,2,3,4 중 하나
    return `index.html${randomNum}`;
}

function getMessage() {
    return 'undefindValue';
}

// 메인 테스트 함수
export default function () {
    const filename = getRandomFilename();
    const message = getMessage();

    const url = `http://localhost:8080/api/error?filename=${filename}&message=${message}`;

    const response = http.get(url, {
        headers: {
            'Content-Type': 'application/json',
        },
    });

    // 응답 상태별 로그
    if (response.status === 200) {
        console.log(`✅ [${filename}] 요청 성공`);
    } else if (response.status === 429) {
        // console.log(`🚫 [${filename}] Rate Limited`);
    } else {
        // console.log(`❌ [${filename}] 응답: ${response.status}`);
    }
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

// === 다른 테스트 시나리오들 ===

// 1. 동일한 파일명으로 반복 테스트 (Rate Limiting 확인)
export function sameFilenameTest() {
    const filename = 'test.html'; // 고정 파일명

    for (let i = 0; i < 10; i++) {
        const message = `error_${i}`;
        const url = `http://localhost:8080/api/error?filename=${filename}&message=${message}`;

        const response = http.get(url);
        console.log(`[${i+1}/10] ${filename}: ${response.status} - ${response.body}`);

        sleep(1); // 1초 간격
    }
}

// 2. 완전히 동일한 요청 반복 (중복 제한 확인)
export function duplicateRequestTest() {
    const filename = 'duplicate.html';
    const message = 'sameError{}';

    for (let i = 0; i < 7; i++) { // 5개 제한이므로 7개 요청
        const url = `http://localhost:8080/api/error?filename=${filename}&message=${message}`;
        const response = http.get(url);

        console.log(`[${i+1}/7] 동일 요청: ${response.status}`);
        if (response.status === 429) {
            console.log(`🚫 ${i+1}번째에서 차단됨!`);
            break;
        }
    }
}