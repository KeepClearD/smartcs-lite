// src/pages/Conversations.tsx
import React, { useEffect, useState } from 'react';
import { Table, Tag, Button, Space, Select, message } from 'antd';
import api from '../utils/api';

const statusColors: Record<string, string> = {
    BOT: 'blue', PENDING: 'orange', AGENT: 'green', CLOSED: 'default',
};

const statusLabels: Record<string, string> = {
    BOT: '机器人', PENDING: '排队中', AGENT: '人工接待', CLOSED: '已关闭',
};

const Conversations: React.FC = () => {
    const [data, setData] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState<string>('');
    const [total, setTotal] = useState(0);
    const [page, setPage] = useState(0);

    const fetchData = async (p = page, s = status) => {
        setLoading(true);
        try {
            const params: any = { page: p, size: 20 };
            if (s) params.status = s;
            const res = await api.get('/api/v1/conversations', { params });
            setData(res.data.data.content);
            setTotal(res.data.data.totalElements);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchData(); }, []);

    const columns = [
        { title: 'ID', dataIndex: 'id', width: 80 },
        { title: '客户', dataIndex: 'customerName', width: 120 },
        {
            title: '状态', dataIndex: 'status', width: 100,
            render: (s: string) => <Tag color={statusColors[s]}>{statusLabels[s]}</Tag>
        },
        { title: '座席', dataIndex: 'agentName', width: 100,
            render: (v: string) => v || '-' },
        { title: '最后消息', dataIndex: 'lastMessage', ellipsis: true },
        { title: '时间', dataIndex: 'createdAt', width: 180,
            render: (v: string) => new Date(v).toLocaleString('zh-CN') },
        {
            title: '操作', width: 120,
            render: (_: any, record: any) => (
                <Space>
                    {record.status === 'PENDING' && (
                        <Button type="primary" size="small"
                                onClick={() => handleAccept(record.id)}>接手</Button>
                    )}
                    {record.status !== 'CLOSED' && (
                        <Button size="small" danger
                                onClick={() => handleClose(record.id)}>关闭</Button>
                    )}
                </Space>
            )
        },
    ];

    const handleAccept = async (id: number) => {
        await api.put(`/api/v1/conversations/${id}/assign?agentId=1`);
        message.success('已接手');
        fetchData();
    };

    const handleClose = async (id: number) => {
        await api.put(`/api/v1/conversations/${id}/close`);
        message.success('已关闭');
        fetchData();
    };

    return (
        <div>
            <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
                <h2 style={{ margin: 0 }}>会话管理</h2>
                <Space>
                    <Select placeholder="筛选状态" allowClear style={{ width: 140 }}
                            value={status || undefined}
                            onChange={(v) => { setStatus(v || ''); fetchData(0, v || ''); }}>
                        <Select.Option value="BOT">机器人</Select.Option>
                        <Select.Option value="PENDING">排队中</Select.Option>
                        <Select.Option value="AGENT">人工接待</Select.Option>
                        <Select.Option value="CLOSED">已关闭</Select.Option>
                    </Select>
                    <Button onClick={() => fetchData()}>刷新</Button>
                </Space>
            </div>
            <Table columns={columns} dataSource={data} rowKey="id"
                   loading={loading} pagination={{
                total, current: page + 1, pageSize: 20,
                onChange: (p) => { setPage(p - 1); fetchData(p - 1); }
            }} />
        </div>
    );
};

export default Conversations;
