#!/usr/bin/env bash
# Fails unless an `adb shell am instrument -r` run passed.
# adb exits 0 even when tests fail or the test process crashes, so the raw status codes in
# the output are the only reliable signal. Codes: 1 started, 0 passed, -1 error, -2 failure,
# -3 ignored, -4 assumption failure; the run ends with `INSTRUMENTATION_CODE: -1` when it
# completes normally.
# Usage: check-instrumentation-result.sh <file with the raw am instrument output>
set -euo pipefail

LOG="${1:?usage: $0 <am-instrument-output>}"

count() { grep -c -E "$1" "$LOG" || true; }

started=$(count 'INSTRUMENTATION_STATUS_CODE: 1')
passed=$(count 'INSTRUMENTATION_STATUS_CODE: 0')
failed=$(count 'INSTRUMENTATION_STATUS_CODE: -[12]')
ignored=$(count 'INSTRUMENTATION_STATUS_CODE: -[34]')

echo "Instrumented tests: started=${started} passed=${passed} failed=${failed} ignored=${ignored}"

# Name every failing test (class#method) from the status blocks.
awk '
  /INSTRUMENTATION_STATUS: class=/ { sub(/.*class=/, ""); gsub(/\r/, ""); cls = $0 }
  /INSTRUMENTATION_STATUS: test=/  { sub(/.*test=/, "");  gsub(/\r/, ""); t = $0 }
  /INSTRUMENTATION_STATUS_CODE: -[12]/ { print "::error::Failed: " cls "#" t }
' "$LOG"

problem=""
if grep -q -E 'INSTRUMENTATION_FAILED|INSTRUMENTATION_RESULT: shortMsg=' "$LOG"; then
  problem="the test process crashed or failed to start: $(grep -m1 -E 'INSTRUMENTATION_FAILED|shortMsg=' "$LOG" | sed -E 's/.*(INSTRUMENTATION_FAILED|shortMsg=)//' | tr -d '\r')"
elif ! grep -q 'INSTRUMENTATION_CODE: -1' "$LOG"; then
  problem="the run did not complete (no INSTRUMENTATION_CODE: -1)"
elif [ "$started" -eq 0 ]; then
  problem="no tests ran"
elif [ "$failed" -gt 0 ]; then
  problem="${failed} test(s) failed"
elif [ $((passed + ignored)) -ne "$started" ]; then
  problem="only $((passed + ignored)) of ${started} started tests finished"
fi

if [ -n "$problem" ]; then
  echo "::error::Instrumented tests did not pass: ${problem}"
  exit 1
fi
echo "All ${passed} instrumented tests passed."
