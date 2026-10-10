import axios from 'axios'

const healthApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export async function createHealthReport(message, conversationId) {
  const { data } = await healthApi.post('/ai/health/report', {
    message,
    conversationId
  })
  return data
}

export function getApiErrorMessage(error) {
  if (error.code === 'ECONNABORTED') {
    return '生成时间有点久，请稍后再试。'
  }

  if (!error.response) {
    return '暂时连不上健康服务，请确认后端已在 8123 端口启动。'
  }

  return error.response.data?.error || '计划生成失败，请稍后再试。'
}
