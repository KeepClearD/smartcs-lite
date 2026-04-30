// src/pages/Agents.tsx
import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, message, Space } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import api from '../utils/api';

const statusColors: Record<string, string> = {
    ONLINE: 'green', BUSY: 'red', AWAY: 'orange', OFFLINE: 'default',
};

const Agents: React.FC = () => {
    const [agents, setAgents] = useState<any[]>([]);
    const [addOpen, setAddOpen] = useState(false);
    const [form] = Form.useForm();

    const fetchAgents = async () => {
        const res = await api.get('/api/v1/agents');
        setAgents(res.data.data);
    };

    useEffect(() => { fetchAgents(); }, []);

    const handleAdd = async () => {
        const values = await form.validateFields();
        await api.post('/api/v1/agents', values);
        message.success('座席已添加');
        setAddOpen(false);
        form.resetFields();
        fetchAgents();
    };

    const handleStatusChange = async (id: number, status: string) => {
        await api.put(`/api/v1/agents/${id}/status?status=${status}`);
        fetchAgents();
    };

    const columns = [
        { title: 'ID', dataIndex: 'id', width: 60 },
        { title: '姓名', dataIndex: 'name', width: 120 },
        { title: '邮箱', dataIndex: 'email', width: 200 },
        { title: '角色', dataIndex: 'role', width: 100,
            render: (v: string) => <Tag color={v === 'ADMIN' ? 'gold' : 'blue'}>{v}</Tag>
        },
        { title: '状态', dataIndex: 'status', width: 100,
            render: (v: string) => <Tag color={statusColors[v]}>{v}</Tag>
        },
        { title: '负载', width: 100,
            render: (_: any, r: any) => `${r.currentLoad}/${r.maxConcurrent}` },
        { title: '操作', width: 200,
            render: (_: any, r: any) => (
                <Space>
                    {r.status === 'OFFLINE' && (
                        <Button size="small" type="primary"
                                onClick={() => handleStatusChange(r.id, 'ONLINE')}>上线</Button>
                    )}
                    {r.status === 'ONLINE' && (
                        <Button size="small" danger
                                onClick={() => handleStatusChange(r.id, 'OFFLINE')}>下线</Button>
                    )}
                </Space>
            )
        },
    ];

    return (
        <div>
            <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
                <h2 style={{ margin: 0 }}>座席管理</h2>
                <Button type="primary" icon={<PlusOutlined />}
                        onClick={() => setAddOpen(true)}>添加座席</Button>
            </div>
            <Table columns={columns} dataSource={agents} rowKey="id" pagination={false} />

            <Modal title="添加座席" open={addOpen} onOk={handleAdd}
                   onCancel={() => setAddOpen(false)}>
                <Form form={form} layout="vertical">
                    <Form.Item name="name" label="姓名" rules={[{ required: true }]}>
                        <Input />
                    </Form.Item>
                    <Form.Item name="email" label="邮箱" rules={[{ required: true, type: 'email' }]}>
                        <Input />
                    </Form.Item>
                    <Form.Item name="role" label="角色" initialValue="AGENT">
                        <Select>
                            <Select.Option value="AGENT">座席</Select.Option>
                            <Select.Option value="ADMIN">管理员</Select.Option>
                        </Select>
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default Agents;
