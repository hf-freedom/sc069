import React, { useState, useEffect } from 'react';
import { billingApi, customerApi } from '../services/api';

function Billing() {
  const [customers, setCustomers] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState('');
  const [bills, setBills] = useState([]);
  const [usages, setUsages] = useState([]);
  const [totalCost, setTotalCost] = useState(0);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [messageType, setMessageType] = useState('success');

  const [dateRange, setDateRange] = useState({
    start: new Date().toISOString().split('T')[0],
    end: new Date().toISOString().split('T')[0],
  });

  useEffect(() => {
    fetchCustomers();
  }, []);

  useEffect(() => {
    if (selectedCustomer) {
      fetchBillingData();
    }
  }, [selectedCustomer, dateRange]);

  const fetchCustomers = async () => {
    try {
      const response = await customerApi.getAll();
      setCustomers(response.data);
    } catch (error) {
      console.error('获取客户列表失败:', error);
    }
  };

  const fetchBillingData = async () => {
    if (!selectedCustomer) return;

    try {
      setLoading(true);
      const [billsRes, usagesRes, costRes] = await Promise.all([
        billingApi.getBills(selectedCustomer),
        billingApi.getUsage(selectedCustomer, dateRange.start, dateRange.end),
        billingApi.getTotalCost(selectedCustomer, dateRange.start, dateRange.end),
      ]);

      setBills(billsRes.data);
      setUsages(usagesRes.data);
      setTotalCost(costRes.data.totalCost);
    } catch (error) {
      console.error('获取账单数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateDaily = async () => {
    try {
      await billingApi.generateDaily();
      setMessage('每日费用生成成功！');
      setMessageType('success');
      fetchBillingData();
    } catch (error) {
      setMessage('操作失败: ' + (error.response?.data || error.message));
      setMessageType('error');
    }
  };

  const handleCheckOverdue = async () => {
    try {
      await billingApi.checkOverdue();
      setMessage('欠费检查完成！');
      setMessageType('success');
      fetchCustomers();
    } catch (error) {
      setMessage('操作失败: ' + (error.response?.data || error.message));
      setMessageType('error');
    }
  };

  const getTypeLabel = (type) => {
    switch (type) {
      case 'VM':
        return '云主机';
      case 'STORAGE':
        return '存储';
      case 'BANDWIDTH':
        return '带宽';
      case 'DATABASE':
        return '数据库';
      default:
        return type;
    }
  };

  const getStatusBadge = (status) => {
    let className = 'status-badge ';
    switch (status) {
      case 'PAID':
        className += 'status-active';
        break;
      case 'PENDING':
        className += 'status-pending';
        break;
      case 'OVERDUE':
        className += 'status-suspended';
        break;
      default:
        className += 'status-stopped';
    }
    return <span className={className}>{status}</span>;
  };

  return (
    <div>
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <h2 style={{ margin: 0, borderBottom: 'none', paddingBottom: 0 }}>费用管理</h2>
            <select
              value={selectedCustomer}
              onChange={e => setSelectedCustomer(e.target.value)}
              style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
            >
              <option value="">请选择客户</option>
              {customers.map(c => (
                <option key={c.id} value={c.id}>{c.name} ({c.id})</option>
              ))}
            </select>
          </div>
          <div style={{ display: 'flex', gap: '10px' }}>
            <button className="btn btn-secondary" onClick={handleGenerateDaily}>
              生成每日费用
            </button>
            <button className="btn btn-warning" onClick={handleCheckOverdue}>
              检查欠费
            </button>
          </div>
        </div>

        {message && (
          <div className={messageType === 'success' ? 'success' : 'error'}>
            {message}
            <button 
              style={{ float: 'right', background: 'none', border: 'none', cursor: 'pointer' }}
              onClick={() => setMessage(null)}
            >✕</button>
          </div>
        )}

        {selectedCustomer && (
          <div style={{ marginBottom: '20px', padding: '15px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
              <div>
                <label style={{ fontWeight: 'bold' }}>开始日期:</label>
                <input
                  type="date"
                  value={dateRange.start}
                  onChange={e => setDateRange({ ...dateRange, start: e.target.value })}
                  style={{ marginLeft: '10px', padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
                />
              </div>
              <div>
                <label style={{ fontWeight: 'bold' }}>结束日期:</label>
                <input
                  type="date"
                  value={dateRange.end}
                  onChange={e => setDateRange({ ...dateRange, end: e.target.value })}
                  style={{ marginLeft: '10px', padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
                />
              </div>
              <div style={{ marginLeft: 'auto' }}>
                <strong>该时间段总费用: </strong>
                <span style={{ 
                  color: totalCost < 0 ? '#dc3545' : '#28a745', 
                  fontSize: '20px', 
                  fontWeight: 'bold' 
                }}>
                  ¥{totalCost.toFixed(2)}
                </span>
              </div>
            </div>
          </div>
        )}
      </div>

      {selectedCustomer && !loading && (
        <>
          <div className="card">
            <h3>每日使用明细</h3>
            {usages.length > 0 ? (
              <table>
                <thead>
                  <tr>
                    <th>使用日期</th>
                    <th>资源ID</th>
                    <th>资源类型</th>
                    <th>使用时长 (小时)</th>
                    <th>每小时费用</th>
                    <th>当日费用</th>
                  </tr>
                </thead>
                <tbody>
                  {usages.map(usage => (
                    <tr key={usage.id}>
                      <td>{usage.usageDate}</td>
                      <td>{usage.resourceId}</td>
                      <td>{getTypeLabel(usage.resourceType)}</td>
                      <td>{usage.usageHours.toFixed(2)}</td>
                      <td>¥{usage.hourlyRate.toFixed(2)}</td>
                      <td style={{ color: '#dc3545', fontWeight: 'bold' }}>¥{usage.dailyCost.toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
                该时间段暂无使用记录
              </div>
            )}
          </div>

          <div className="card">
            <h3>账单列表</h3>
            {bills.length > 0 ? (
              <table>
                <thead>
                  <tr>
                    <th>账单ID</th>
                    <th>账单日期</th>
                    <th>总金额</th>
                    <th>状态</th>
                    <th>创建时间</th>
                  </tr>
                </thead>
                <tbody>
                  {bills.map(bill => (
                    <tr key={bill.id}>
                      <td>{bill.id}</td>
                      <td>{bill.billingDate}</td>
                      <td style={{ color: '#dc3545', fontWeight: 'bold' }}>¥{bill.totalAmount.toFixed(2)}</td>
                      <td>{getStatusBadge(bill.status)}</td>
                      <td>{new Date(bill.createdAt).toLocaleString('zh-CN')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
                暂无账单记录
              </div>
            )}
          </div>
        </>
      )}

      {!selectedCustomer && (
        <div className="card">
          <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
            请选择一个客户查看费用信息
          </div>
        </div>
      )}

      <div className="card">
        <h3>欠费处理说明</h3>
        <div style={{ marginTop: '10px' }}>
          <p><strong>欠费处理流程:</strong></p>
          <ol style={{ marginLeft: '20px', marginTop: '10px' }}>
            <li>
              <strong>账户余额不足:</strong> 当客户账户余额为负数时，系统自动将客户状态设置为 <span className="status-badge status-restricted">RESTRICTED</span>（限制状态）
            </li>
            <li style={{ marginTop: '10px' }}>
              <strong>限制状态:</strong> 限制状态下，客户的所有资源会被限制使用，但不会立即停机
            </li>
            <li style={{ marginTop: '10px' }}>
              <strong>长期欠费:</strong> 当客户账户余额低于 -¥1000 时，系统自动将客户状态设置为 <span className="status-badge status-suspended">SUSPENDED</span>（停机状态）
            </li>
            <li style={{ marginTop: '10px' }}>
              <strong>停机状态:</strong> 停机状态下，客户的所有资源会被停止运行
            </li>
            <li style={{ marginTop: '10px' }}>
              <strong>恢复状态:</strong> 客户充值后，若账户余额恢复为正数，系统会自动将客户状态恢复为 <span className="status-badge status-active">ACTIVE</span>（正常状态）
            </li>
          </ol>
        </div>
      </div>
    </div>
  );
}

export default Billing;
