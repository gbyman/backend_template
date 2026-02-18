#!/bin/bash

# Jasypt 암호화 테스트 스크립트

SECRET_KEY=${1:-"test-secret-key"}
PLAIN_TEXT=${2:-"postgres"}

echo "==================================="
echo "Jasypt 암호화 테스트"
echo "==================================="
echo "암호화 키: $SECRET_KEY"
echo "평문: $PLAIN_TEXT"
echo "-----------------------------------"

# Gradle로 JasyptEncryptUtil 실행
./gradlew :module-api:bootRun --args="jasypt-encrypt $SECRET_KEY $PLAIN_TEXT" --quiet 2>&1 | grep -A 20 "Jasypt Encryption"
