import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: 10,
  duration: "30s",
};

export default function () {
  const url = "http://localhost:8080/api/recommendation?userId=1&limit=10";
  const payload = JSON.stringify({ shopIds: null, featureFilter: null });
  const params = {
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer ADD_TOKEN_HERE",
    },
  };
  let res = http.post(url, payload, params);
  check(res, { "status is 200": (r) => r.status === 200 });
  sleep(1);
}
