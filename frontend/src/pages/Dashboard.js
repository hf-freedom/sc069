import React, { useState, useEffect } from 'react';
import { customerApi, resourceApi } from '../services/api';

function Dashboard() {
  const [stats, setStats] = useState({
    customers: 0,
    resources: 0,
    runningResources: 0,
    totalBalance: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [customersRes, resourcesRes] = await Promise.all([
          customerApi.getAll(),
          resourceApi.getAll(),
        ]);

        const customers = customersRes.data;
        const resources = resourcesRes.data;
        const runningResources = resources.filter(r => r.status === 'RUNNING').length;
        const totalBalance = customers.reduce((sum, c) => sum + (c.accountBalance || 0), 0);

        setStats({
          customers: customers.length,
          resources: resources.length,
          runningResources,
          totalBalance,
        });
      } catch (error) {
        console.error('获取统计数据失败:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
    const interval = setInterval(fetchStats, 30000);
    
    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  return (
    <div>
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-value">{stats.customers}</div>
          <div className="stat-label">客户总数</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{stats.resources}</div>
          <div className="stat-label">资源总数</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{stats.runningResources}</div>
          <div className="stat-label">运行中资源</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">¥{stats.totalBalance.toFixed(2)}</div>
          <div className="stat-label">总账户余额</div>
        </div>
      </div>

      <div className="card">
        <h2>系统概览</h2>
        <p>欢迎使用云资源管理系统！</p>
        <p>本系统提供以下功能：</p>
        <ul style={{ marginTop: '10px', marginLeft: '20px' }}>
          <li>客户管理：管理客户账户、余额和配额</li>
          <li>资源管理：开通、启动、停止、释放云资源</li>
          <li>监控中心：实时监控资源使用情况</li>
          <li>费用管理：查看账单和使用费用</li>
        </ul>
        
        <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
          <h3>默认测试客户</h3>
          <p><strong>客户ID: C001</strong></p>
          <p>名称: 张三</p>
          <p>账户余额: ¥5000.00</p>
          <p>信用额度: ¥10000.00</p>
          <br/>
          <p><strong>客户ID: C002</strong></p>
          <p>名称: 李四</p>
          <p>账户余额: ¥1000.00</p>
          <p>信用额度: ¥5000.00</p>
        </div>
      </div>

      <div className="card">
        <h2>资源类型</h2>
        <div className="stats-grid" style={{ marginTop: '10px' }}>
          <div className="stat-card">
            <div className="stat-label" style={{ fontSize: '16px', fontWeight: 'bold' }}>云主机 (VM)</div>
            <div className="stat-label" style={{ marginTop: '10px' }}>CPU: ¥0.5/核/小时</div>
            <div className="stat-label">内存: ¥0.2/GB/小时</div>
          </div>
          <div className="stat-card">
            <div className="stat-label" style={{ fontSize: '16px', fontWeight: 'bold' }}>存储 (Storage)</div>
            <div className="stat-label" style={{ marginTop: '10px' }}>¥0.01/GB/小时</div>
          </div>
          <div className="stat-card">
            <div className="stat-label" style={{ fontSize: '16px', fontWeight: 'bold' }}>带宽 (Bandwidth)</div>
            <div className="stat-label" style={{ marginTop: '10px' }}>¥0.1/Mbps/小时</div>
          </div>
          <div className="stat-card">
            <div className="stat-label" style={{ fontSize: '16px', fontWeight: 'bold' }}>数据库 (Database)</div>
            <div className="stat-label" style={{ marginTop: '10px' }}>MySQL: ¥0.3/小时起</div>
            <div className="stat-label">其他: ¥0.5/小时起</div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
