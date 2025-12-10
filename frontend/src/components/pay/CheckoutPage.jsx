import { loadTossPayments } from "@tosspayments/tosspayments-sdk"
import { useEffect, useState } from "react"
import {
  Box,
  Container,
  Typography,
  Paper,
  Button,
  Checkbox,
  FormControlLabel,
  Divider,
  Chip,
  CircularProgress,
  Alert,
  Fade
} from '@mui/material'
import Grid from '@mui/material/Grid'
import PaymentIcon from '@mui/icons-material/Payment'
import LocalOfferIcon from '@mui/icons-material/LocalOffer'

const clientKey = "test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm"
const customerKey = "D7hgLPbsxvRaTpBQ4Zcun"

export function CheckoutPage() {
  const [amount, setAmount] = useState({
    currency: "KRW",
    value: 100,
  })
  const [ready, setReady] = useState(false)
  const [widgets, setWidgets] = useState(null)
  const [couponApplied, setCouponApplied] = useState(false)
  const [isProcessing, setIsProcessing] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    async function fetchPaymentWidgets() {
      try {
        const tossPayments = await loadTossPayments(clientKey)
        const widgets = tossPayments.widgets({
          customerKey,
        })
        setWidgets(widgets)
      } catch (err) {
        setError("결제 위젯을 불러오는 데 실패했습니다.")
        console.error(err)
      }
    }

    fetchPaymentWidgets()
  }, [])

  useEffect(() => {
    async function renderPaymentWidgets() {
      if (widgets == null) {
        return
      }

      await widgets.setAmount(amount)

      await Promise.all([
        widgets.renderPaymentMethods({
          selector: "#payment-method",
          variantKey: "DEFAULT",
        }),
        widgets.renderAgreement({
          selector: "#agreement",
          variantKey: "AGREEMENT",
        }),
      ])

      setReady(true)
    }

    renderPaymentWidgets()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [widgets])

  useEffect(() => {
    if (widgets == null) {
      return
    }

    widgets.setAmount(amount)
  }, [widgets, amount])

  const handleCouponChange = (event) => {
    const checked = event.target.checked
    setCouponApplied(checked)
    setAmount(prev => ({
      ...prev,
      value: checked ? prev.value - 5000 : prev.value + 5000
    }))
  }

  const handlePayment = async () => {
    setIsProcessing(true)
    setError(null)

    try {
      await widgets.requestPayment({
        orderId: "TL3ke6yo0eqYdXbQ3D_y4",
        orderName: "프리미엄 구독권",
        successUrl: window.location.origin + "/success",
        failUrl: window.location.origin + "/fail",
        customerEmail: "customer123@gmail.com",
        customerName: "홍길동",
        customerMobilePhone: "01012341234",
      })
    } catch (err) {
      setError("결제 처리 중 오류가 발생했습니다.")
      console.error(err)
    } finally {
      setIsProcessing(false)
    }
  }

  const formatPrice = (price) => {
    return new Intl.NumberFormat('ko-KR').format(price)
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #2D5A47 0%, #1E3D30 100%)',
        pt: { xs: 10, sm: 12 },
        pb: 6,
      }}
    >
      <Container maxWidth="lg">
        {/* 에러 메시지 */}
        {error && (
          <Fade in={!!error}>
            <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
              {error}
            </Alert>
          </Fade>
        )}

        <Grid container spacing={3}>
          {/* 왼쪽 영역 */}
          <Grid size={{ xs: 12, md: 7 }}>
            {/* 왼쪽 상단: 결제 수단 선택 */}
            <Paper
              elevation={0}
              sx={{
                p: 3,
                mb: 3,
                borderRadius: 3,
                bgcolor: 'rgba(255, 255, 255, 0.95)',
                height: 'fit-content',
              }}
            >
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, color: 'primary.main' }}>
                결제 수단 선택
              </Typography>

              {!ready && (
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                  <CircularProgress color="primary" />
                </Box>
              )}

              <Box
                id="payment-method"
                sx={{
                  '& > div': {
                    borderRadius: 2,
                  }
                }}
              />

              {/* 이용약관 */}
              <Divider sx={{ my: 3 }} />
              <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 2 }}>
                이용약관
              </Typography>
              <Box id="agreement" />
            </Paper>

            {/* 왼쪽 하단: 쿠폰 정보 */}
            <Paper
              elevation={0}
              sx={{
                p: 3,
                borderRadius: 3,
                bgcolor: couponApplied ? 'rgba(255, 255, 255, 0.98)' : 'rgba(255, 255, 255, 0.95)',
                border: couponApplied ? '2px solid' : 'none',
                borderColor: 'secondary.main',
                transition: 'all 0.2s ease-in-out',
              }}
            >
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, color: 'primary.main' }}>
                쿠폰 및 할인
              </Typography>

              <FormControlLabel
                control={
                  <Checkbox
                    checked={couponApplied}
                    onChange={handleCouponChange}
                    disabled={!ready}
                    color="primary"
                  />
                }
                label={
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                    <LocalOfferIcon sx={{ fontSize: 24, color: couponApplied ? 'secondary.main' : 'text.secondary' }} />
                    <Box>
                      <Typography variant="body1" sx={{ fontWeight: 500 }}>
                        5,000원 할인 쿠폰
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        첫 결제 할인 쿠폰 (자동 적용 가능)
                      </Typography>
                    </Box>
                  </Box>
                }
                sx={{ m: 0, width: '100%', py: 1 }}
              />

              {couponApplied && (
                <Box sx={{ mt: 2, p: 2, bgcolor: 'rgba(45, 90, 71, 0.08)', borderRadius: 2 }}>
                  <Typography variant="body2" color="primary.main" sx={{ fontWeight: 500 }}>
                    쿠폰이 적용되었습니다! -5,000원 할인
                  </Typography>
                </Box>
              )}
            </Paper>
          </Grid>

          {/* 오른쪽 영역 */}
          <Grid size={{ xs: 12, md: 5 }}>
            {/* 오른쪽 상단: 결제 정보 */}
            <Paper
              elevation={0}
              sx={{
                p: 3,
                mb: 3,
                borderRadius: 3,
                bgcolor: 'rgba(255, 255, 255, 0.95)',
              }}
            >
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 3, color: 'primary.main' }}>
                주문 정보
              </Typography>

              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Box>
                  <Typography variant="body1" sx={{ fontWeight: 600 }}>
                    프리미엄 구독권
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    무제한 도서 열람 / 1개월
                  </Typography>
                </Box>
                <Chip
                  label="구독"
                  size="small"
                  color="primary"
                  variant="outlined"
                />
              </Box>

              <Divider sx={{ my: 2 }} />

              {/* 가격 정보 */}
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2" color="text.secondary">
                  상품 금액
                </Typography>
                <Typography variant="body2">
                  {formatPrice(couponApplied ? amount.value + 5000 : amount.value)}원
                </Typography>
              </Box>

              {couponApplied && (
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2" color="error.main">
                    쿠폰 할인
                  </Typography>
                  <Typography variant="body2" color="error.main">
                    -5,000원
                  </Typography>
                </Box>
              )}

              <Divider sx={{ my: 2 }} />

              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                  총 결제 금액
                </Typography>
                <Typography variant="h4" sx={{ fontWeight: 700, color: 'primary.main' }}>
                  {formatPrice(amount.value)}원
                </Typography>
              </Box>
            </Paper>

            {/* 오른쪽 하단: 결제 버튼 */}
            <Paper
              elevation={0}
              sx={{
                p: 3,
                borderRadius: 3,
                bgcolor: 'rgba(255, 255, 255, 0.95)',
              }}
            >
              <Button
                fullWidth
                variant="contained"
                size="large"
                disabled={!ready || isProcessing}
                onClick={handlePayment}
                startIcon={isProcessing ? <CircularProgress size={20} color="inherit" /> : <PaymentIcon />}
                sx={{
                  py: 2,
                  fontSize: '1.1rem',
                  fontWeight: 600,
                  borderRadius: 2,
                  bgcolor: 'primary.main',
                  color: 'white',
                  boxShadow: '0 4px 14px rgba(45, 90, 71, 0.4)',
                  '&:hover': {
                    bgcolor: 'primary.dark',
                    boxShadow: '0 6px 20px rgba(45, 90, 71, 0.5)',
                  },
                  '&:disabled': {
                    bgcolor: 'grey.300',
                    color: 'grey.500',
                  }
                }}
              >
                {isProcessing ? '결제 처리중...' : `${formatPrice(amount.value)}원 결제하기`}
              </Button>

              <Box sx={{ mt: 2, textAlign: 'center' }}>
                <Typography variant="caption" color="text.secondary">
                  결제 시 서비스 이용약관에 동의하게 됩니다.
                </Typography>
              </Box>

              {/* 결제 안내 */}
              <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 2 }}>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
                  • 결제 완료 후 즉시 서비스가 활성화됩니다.
                </Typography>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
                  • 구독은 매월 자동 갱신됩니다.
                </Typography>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                  • 언제든지 구독을 취소할 수 있습니다.
                </Typography>
              </Box>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </Box>
  )
}

export default CheckoutPage
