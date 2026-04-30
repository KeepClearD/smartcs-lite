// src/pages/Dashboard.tsx
import React, { useEffect, useState } from 'react';
import { Card, Col, Row, Statistic } from 'antd';
import {
    MessageOutlined,
    TeamOutlined,
    RobotOutlined,
    CheckCircleOutlined,
} from '@ant-design/icons';
import api from '../utils/api';

interface Analytics {
    totalConversations: number;
    todayConversations: number;
    totalMessages: number;
    todayMessages: number;
    activeConversations: number;
    onlineAgents: number;
    botResolutionRate: number;
}

const Dashboard: React.FC = () => {
    const [data, setData] = useState<Analytics | null>(null);

    useEffect(() => {
        api.get('/api/v1/analytics/overview').then(res => setData(res.data.data));
    }, []);

    if (!data) return <div>加载中...</div>;

    return (
        <div>
            <h2 style={{ marginBottom: 24 }}>仪表盘</h2>
            <Row gutter={[16, 16]}>
                <Col xs={12} md={6}>
                    <Card>
                        <Statistic title="今日会话" value={data.todayConversations}
                                   prefix={<MessageOutlined />} valueStyle={{ color: '#1B4D3E' }} />
                    </Card>
                </Col>
                <Col xs={12} md={6}>
                    <Card>
                        <Statistic title="活跃会话" value={data.activeConversations}
                                   prefix={<CheckCircleOutlined />} valueStyle={{ color: '#52c41a' }} />
                    </Card>
                </Col>
                <Col xs={12} md={6}>
                    <Card>
                        <Statistic title="在线座席" value={data.onlineAgents}
                                   prefix={<TeamOutlined />} valueStyle={{ color: '#1890ff' }} />
                    </Card>
                </Col>
                <Col xs={12} md={6}>
                    <Card>
                        <Statistic title="机器人解决率" value={data.botResolutionRate * 100}
                                   suffix="%" prefix={<RobotOutlined />}
                                   valueStyle={{ color: '#faad14' }} />
                    </Card>
                </Col>
            </Row>
        </div>
    );
};

export default Dashboard;
