import { useNavigate } from "react-router-dom";

// 커스텀 훅으로 변경
export const useHandleEvent = (setDrawerOpen = null) => {
    const navigate = useNavigate();

    const handleEvent = (value, e) => {
        if(e !== null && e !== undefined) e.preventDefault()

        // 스크롤 최상단으로 이동
        window.scrollTo(0, 0);

        if (value !== null) {
            // 템플릿 섹션인 경우 section 파라미터 추가
                navigate(`/${value}`);
        } else {
            window.location.reload();
        }
        if (setDrawerOpen) {
            setDrawerOpen(false);
        }
    };

    return handleEvent;
};

export const validateAndExecute = (formData, onSuccess, options = {}) => {
    const { minPasswordLength = 8, errorMessage = "입력 형식을 확인해주세요." } = options;

    // 비밀번호 길이 검사
    if (formData.password && formData.password.length < minPasswordLength) {
        alert(errorMessage);
        return false;
    }
    if (formData.verifyCode && formData.verifyCode.length !== 6) {
        alert(errorMessage);
        return false;
    }

    // 빈 값 검사
    const hasEmptyValue = Object.values(formData).some(
        value => value === null || value === '' || value === undefined
    );

    if (hasEmptyValue) {
        alert(errorMessage);
        return false;
    }

    // 검증 성공 시 콜백 실행
    if (onSuccess) {
        onSuccess(formData);
    }
    return true;
}