// src/pages/KnowledgeBase.tsx
import React, { useEffect, useState } from 'react';
import { Table, Button, Modal, Form, Input, Space, Tag, message, Upload, Tabs } from 'antd';
import { UploadOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import api from '../utils/api';

const KnowledgeBase: React.FC = () => {
    const [kbs, setKbs] = useState<any[]>([]);
    const [selectedKb, setSelectedKb] = useState<number | null>(null);
    const [faqs, setFaqs] = useState<any[]>([]);
    const [docs, setDocs] = useState<any[]>([]);
    const [addFaqOpen, setAddFaqOpen] = useState(false);
    const [addKbOpen, setAddKbOpen] = useState(false);
    const [form] = Form.useForm();
    const [kbForm] = Form.useForm();

    const fetchKbs = async () => {
        const res = await api.get('/api/v1/knowledge-bases');
        setKbs(res.data.data);
        if (res.data.data.length > 0 && !selectedKb) {
            setSelectedKb(res.data.data[0].id);
        }
    };

    const fetchFaqs = async (kbId: number) => {
        const res = await api.get(`/api/v1/knowledge-bases/${kbId}/faqs`);
        setFaqs(res.data.data);
    };

    const fetchDocs = async (kbId: number) => {
        const res = await api.get(`/api/v1/knowledge-bases/${kbId}/documents`);
        setDocs(res.data.data);
    };

    useEffect(() => { fetchKbs(); }, []);
    useEffect(() => {
        if (selectedKb) { fetchFaqs(selectedKb); fetchDocs(selectedKb); }
    }, [selectedKb]);

    const handleAddFaq = async () => {
        const values = await form.validateFields();
        await api.post(`/api/v1/knowledge-bases/${selectedKb}/faqs`, values);
        message.success('FAQ 已添加');
        setAddFaqOpen(false);
        form.resetFields();
        fetchFaqs(selectedKb!);
    };

    const handleAddKb = async () => {
        const values = await kbForm.validateFields();
        await api.post('/api/v1/knowledge-bases', null, { params: values });
        message.success('知识库已创建');
        setAddKbOpen(false);
        kbForm.resetFields();
        fetchKbs();
    };

    const handleDeleteFaq = async (faqId: number) => {
        await api.delete(`/api/v1/knowledge-bases/${selectedKb}/faqs/${faqId}`);
        message.success('已删除');
        fetchFaqs(selectedKb!);
    };

    const handleUpload = async (file: any) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('title', file.name);
        await api.post(`/api/v1/knowledge-bases/${selectedKb}/documents`, formData);
        message.success('文档已上传，正在处理...');
        fetchDocs(selectedKb!);
        return false;
    };

    const faqColumns = [
        { title: '问题', dataIndex: 'question', ellipsis: true },
        { title: '答案', dataIndex: 'answer', ellipsis: true },
        { title: '分类', dataIndex: 'category', width: 100,
            render: (v: string) => v ? <Tag>{v}</Tag> : '-' },
        { title: '命中', dataIndex: 'hitCount', width: 80 },
        { title: '操作', width: 80,
            render: (_: any, r: any) => (
                <Button type="link" danger size="small" icon={<DeleteOutlined />}
                        onClick={() => handleDeleteFaq(r.id)} />
            )
        },
    ];

    const docColumns = [
        { title: '文档名', dataIndex: 'title' },
        { title: '类型', dataIndex: 'fileType', width: 80 },
        { title: '状态', dataIndex: 'status', width: 100,
            render: (s: string) => (
                <Tag color={s === 'READY' ? 'green' : s === 'FAILED' ? 'red' : 'blue'}>{s}</Tag>
            )
        },
        { title: '分块数', dataIndex: 'chunkCount', width: 80 },
        { title: '时间', dataIndex: 'createdAt', width: 180,
            render: (v: string) => new Date(v).toLocaleString('zh-CN') },
    ];

    return (
        <div>
            <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
                <h2 style={{ margin: 0 }}>知识库管理</h2>
                <Button type="primary" icon={<PlusOutlined />}
                        onClick={() => setAddKbOpen(true)}>新建知识库</Button>
            </div>

            {/* 知识库选择 */}
            <Space wrap style={{ marginBottom: 16 }}>
                {kbs.map(kb => (
                    <Button key={kb.id}
                            type={selectedKb === kb.id ? 'primary' : 'default'}
                            onClick={() => setSelectedKb(kb.id)}>
                        {kb.name}
                    </Button>
                ))}
            </Space>

            {selectedKb && (
                <Tabs items={[
                    {
                        key: 'faq',
                        label: 'FAQ 管理',
                        children: (
                            <>
                                <div style={{ marginBottom: 16 }}>
                                    <Button type="primary" icon={<PlusOutlined />}
                                            onClick={() => setAddFaqOpen(true)}>添加 FAQ</Button>
                                </div>
                                <Table columns={faqColumns} dataSource={faqs} rowKey="id"
                                       pagination={{ pageSize: 10 }} />
                            </>
                        )
                    },
                    {
                        key: 'docs',
                        label: '文档管理',
                        children: (
                            <>
                                <div style={{ marginBottom: 16 }}>
                                    <Upload beforeUpload={handleUpload} showUploadList={false}
                                            accept=".pdf,.doc,.docx,.txt,.md,.html">
                                        <Button type="primary" icon={<UploadOutlined />}>上传文档</Button>
                                    </Upload>
                                </div>
                                <Table columns={docColumns} dataSource={docs} rowKey="id"
                                       pagination={{ pageSize: 10 }} />
                            </>
                        )
                    }
                ]} />
            )}

            {/* 添加 FAQ 弹窗 */}
            <Modal title="添加 FAQ" open={addFaqOpen} onOk={handleAddFaq}
                   onCancel={() => setAddFaqOpen(false)}>
                <Form form={form} layout="vertical">
                    <Form.Item name="question" label="问题" rules={[{ required: true }]}>
                        <Input.TextArea rows={2} placeholder="用户可能会问的问题" />
                    </Form.Item>
                    <Form.Item name="answer" label="答案" rules={[{ required: true }]}>
                        <Input.TextArea rows={4} placeholder="标准回答" />
                    </Form.Item>
                    <Form.Item name="category" label="分类">
                        <Input placeholder="如：退款、物流、售后" />
                    </Form.Item>
                </Form>
            </Modal>

            {/* 新建知识库弹窗 */}
            <Modal title="新建知识库" open={addKbOpen} onOk={handleAddKb}
                   onCancel={() => setAddKbOpen(false)}>
                <Form form={kbForm} layout="vertical">
                    <Form.Item name="name" label="名称" rules={[{ required: true }]}>
                        <Input placeholder="知识库名称" />
                    </Form.Item>
                    <Form.Item name="description" label="描述">
                        <Input.TextArea rows={2} />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default KnowledgeBase;
