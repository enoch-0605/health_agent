# 风向健康 · 青春手账前端

基于 Vue 3、Vite 和 Axios 的单页前端。开发环境会把 `/api` 请求代理到 `http://localhost:8123`。

## 启动

```powershell
npm install
npm run dev
```

访问 `http://localhost:5173`。AI 健康计划功能需要先启动仓库根目录中的 Spring Boot 服务，并配置 DashScope API Key。

## 构建

```powershell
npm run build
```

如前后端部署在不同地址，可复制 `.env.example` 为 `.env` 并修改 `VITE_API_BASE_URL`。
