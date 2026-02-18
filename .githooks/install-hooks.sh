#!/bin/bash

# Git Hooks 설치 스크립트
# .githooks/ 디렉토리의 훅 스크립트를 .git/hooks/로 복사하고 실행 권한 부여

echo "🔧 Git Hooks 설치 중..."

# 프로젝트 루트 디렉토리 확인
if [ ! -d ".git" ]; then
    echo "❌ .git 디렉토리를 찾을 수 없습니다."
    echo "   프로젝트 루트 디렉토리에서 실행해주세요."
    exit 1
fi

# .githooks 디렉토리 확인
if [ ! -d ".githooks" ]; then
    echo "❌ .githooks 디렉토리를 찾을 수 없습니다."
    exit 1
fi

# 설치할 훅 목록
HOOKS=("commit-msg" "pre-push")

# 각 훅 스크립트 복사 및 실행 권한 부여
for hook in "${HOOKS[@]}"; do
    if [ -f ".githooks/$hook" ]; then
        cp ".githooks/$hook" ".git/hooks/$hook"
        chmod +x ".git/hooks/$hook"
        echo "✅ $hook 설치 완료"
    else
        echo "⚠️  .githooks/$hook 파일을 찾을 수 없습니다."
    fi
done

echo ""
echo "✅ Git Hooks 설치가 완료되었습니다!"
echo ""
echo "📋 설치된 훅:"
echo "   - commit-msg: Conventional Commits 형식 검증"
echo "   - pre-push: main/prod 브랜치 직접 push 방지"
echo ""
echo "💡 테스트 방법:"
echo "   git commit -m \"wrong format\"  # ❌ 실패"
echo "   git commit -m \"feat: add feature\"  # ✅ 성공"
echo ""
echo "   git checkout main"
echo "   git push  # ❌ main 브랜치 push 차단"
echo ""

exit 0
