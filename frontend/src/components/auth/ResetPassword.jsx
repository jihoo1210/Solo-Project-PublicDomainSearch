import React, { useState } from "react";
import {
  Box,
  TextField,
  Button,
  Typography,
  Grid,
  Divider,
  Stepper,
  StepLabel,
  Step,
} from "@mui/material";
import { validateAndExecute } from "../../util/Handle";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import axiosApi from "../../api/AxiosApi";

const ResetPassword = () => {
  const [formData, setFormData] = useState({ email: "", password: "" });
  const [step, setStep] = useState(0);
  const navigate = useNavigate();

  const steps = ['인증코드 전송', '이메일 인증', '비밀번호 재설정']

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    switch(step) {
        case 0: validateAndExecute(formData, (data) => {
            axiosApi.post(data, "/auth/checkEmailAndSendCode")
        })
        break;
        case 1: validateAndExecute(formData, (data) => {
            axiosApi.post(data, "/auth/checkCode")
        })
        break;
        case 2: validateAndExecute(formData, (data) => {
            axios.put(data, "/auth/resetPassword")
            navigate('/login')
        })
        break;
        default: setStep(0);
    }
    setStep(prev => prev + 1)
  };

  return (
    <Grid container sx={{ minHeight: "100vh" }}>
      {/* 좌측: 이미지/브랜딩 영역 */}
      <Grid
        size={{ xs: 12, md: 6 }}
        sx={{
          background: "linear-gradient(135deg, #2D5A47 0%, #4A7C68 100%)",
          display: { xs: "none", md: "flex" },
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
          color: "white",
          p: 6,
        }}
      >
        <Typography variant="h3" sx={{ fontWeight: 700, mb: 2 }}>
          환영합니다
        </Typography>
        <Typography
          variant="h6"
          sx={{ opacity: 0.9, textAlign: "center", maxWidth: 400 }}
        >
          새로운 경험을 시작하세요. 간편하게 가입하고 서비스를 이용해보세요.
        </Typography>
      </Grid>

      {/* 우측: 폼 영역 */}
      <Grid
        size={{ xs: 12, md: 6 }}
        sx={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          p: 4,
        }}
      >
        <Box sx={{ width: "100%", maxWidth: 400 }}>
          <Typography variant="h4" sx={{ mb: 1, fontWeight: 600 }}>
            비밀번호 변경
          </Typography>
          <Typography color="text.secondary" sx={{ mb: 4 }}>
            계정의 비밀번호를 변경하세요.
          </Typography>

          
          <Stepper activeStep={step} sx={{ mb: 4 }} alternativeLabel>
            {steps.map((label) => (
              <Step key={label}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="이메일"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              margin="normal"
              variant="outlined"
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={step > 0}
              sx={{ mt: 3, py: 1.5 }}
            >
              인증코드 전송
            </Button>
            <Divider />
            {step > 0 && (
              <>
                <TextField
                  fullWidth
                  label="인증코드"
                  name="code"
                  type="text"
                  value={formData.code}
                  onChange={handleChange}
                  margin="normal"
                  variant="outlined"
                />
                <Button
                  type="submit"
                  fullWidth
                  variant="contained"
                  size="large"
                  disabled={step > 1}
                  sx={{ mt: 3, py: 1.5 }}
                >
                  인증코드 확인
                </Button>
              </>
            )}
            {step > 1 && (
              <>
                <TextField
                  fullWidth
                  label="비밀번호"
                  name="password"
                  type="password"
                  value={formData.password}
                  onChange={handleChange}
                  margin="normal"
                  variant="outlined"
                />
                <Divider />
                <TextField
                  fullWidth
                  label="비밀번호 확인"
                  name="passwordCheck"
                  type="password"
                  value={formData.passwordCheck}
                  onChange={handleChange}
                  margin="normal"
                  variant="outlined"
                />
                <Button
                  type="submit"
                  fullWidth
                  variant="contained"
                  size="large"
                  sx={{ mt: 3, py: 1.5 }}
                >
                  비밀번호 재설정
                </Button>
              </>
            )}
          </Box>
        </Box>
      </Grid>
    </Grid>
  );
};

export default ResetPassword;
