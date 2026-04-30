// src/layouts/MainLayout.tsx
import React from 'react';
import { Layout, Menu } from 'antd';
import {
    DashboardOutlined,
    MessageOutlined,
    BookOutlined,
    TeamOutlined,
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';

const { Header, Sider, Content } = Layout;

const menuItems = [
    { key: '/dashboard', icon: <DashboardOutlined />, label: '仪表盘' },
    { key: '/conversations', icon: <MessageOutlined />, label: '会话管理' },
    { key: '/knowledge', icon: <BookOutlined />, label: '知识库' },
    { key: '/agents', icon: <TeamOutlined />, label: '座席管理' },
];

const MainLayout: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider theme="light" width={220}>
                <div style={{
                    height: 64, display: 'flex', alignItems: 'center',
                    justifyContent: 'center', fontSize: 20, fontWeight: 700,
                    color: '#1B4D3E', borderBottom: '1px solid #f0f0f0'
                }}>
                    SmartCS
                </div>
                <Menu
                    mode="inline"
                    selectedKeys={[location.pathname]}
                    items={menuItems}
                    onClick={({ key }) => navigate(key)}
                    style={{ borderRight: 0 }}
                />
            </Sider>
            <Layout>
                <Header style={{
                    background: '#fff', padding: '0 24px',
                    borderBottom: '1px solid #f0f0f0',
                    display: 'flex', alignItems: 'center', justifyContent: 'flex-end'
                }}>
                    <span style={{ color: '#666' }}>管理员</span>
                </Header>
                <Content style={{ margin: 24, padding: 24, background: '#fff', borderRadius: 8 }}>
                    <Outlet />
                </Content>
            </Layout>
        </Layout>
    );
};

export default MainLayout;
