// src/App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, theme } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import MainLayout from './layouts/MainLayout';
import Dashboard from './pages/Dashboard';
import Conversations from './pages/Conversations';
import KnowledgeBase from './pages/KnowledgeBase';
import Agents from './pages/Agents';

const App: React.FC = () => (
    <ConfigProvider locale={zhCN} theme={{
        token: { colorPrimary: '#1B4D3E' },
        algorithm: theme.defaultAlgorithm,
    }}>
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<MainLayout />}>
                    <Route index element={<Navigate to="/dashboard" replace />} />
                    <Route path="dashboard" element={<Dashboard />} />
                    <Route path="conversations" element={<Conversations />} />
                    <Route path="knowledge" element={<KnowledgeBase />} />
                    <Route path="agents" element={<Agents />} />
                </Route>
            </Routes>
        </BrowserRouter>
    </ConfigProvider>
);

export default App;
