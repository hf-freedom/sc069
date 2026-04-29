import React, { useState, useEffect } from 'react';
import { customerApi } from '../services/api';

function Customers() {
  const [customers, setCustomers] = useState([]);
  const [quotas, setQuotas] = useState({});
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showRechargeModal, setShowRechargeModal] = useState(false);
  const [showQuotaModal, setShowQuotaModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [message, setMessage] = useState(null);
  const [messageType, setMessageType] = useState('success');

  const [newCustomer, setNewCustomer] = useState({
    name: '',
    email: '',
    initialBalance: 0,
    creditLimit: 5000,
    vmQuota: 10,
    storageQuota: 1000,
    bandwidthQuota: 1000,
    databaseQuota: 5,
  });

  const [rechargeAmount, setRechargeAmount] = useState(0);

  const [quotaForm, setQuotaForm] = useState({
    vmQuota: 0,
    storageQuota: 0,
    bandwidthQuota: 0,
    databaseQuota: 0,
  });

  useEffect(() => {
    fetchCustomers();
  }, []);

  const fetchCustomers = async () => {
    try {
      setLoading(true);
      const response = await customerApi.getAll();
      setCustomers(response.data);
      
      const newQuotas = {};
      for (const customer of response.data) {
        if (customer.quota) {
          newQuotas[customer.id] = customer.quota;
        } else {
          try {
            const quotaRes = await customerApi.getQuota(customer.id);
            newQuotas[customer.id] = quotaRes.data;
          } catch (e) {
            console.error('获取配额失败:', e);
          }
        }
      }
      setQuotas(newQuotas);
    } catch (error) {
      console.error('获取客户列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateCustomer = async (e) => {
    e.preventDefault();
    try {
      const customerData = {
        name: newCustomer.name,
        email: newCustomer.email,
        initialBalance: newCustomer.initialBalance,
        creditLimit: newCustomer.creditLimit,
      };

      const response = await customerApi.create(customerData);
      const newCustomerId = response.data.id;

      const quotaData = {
        vmQuota: newCustomer.vmQuota,
        storageQuota: newCustomer.storageQuota,
        bandwidthQuota: newCustomer.bandwidthQuota,
        databaseQuota: newCustomer.databaseQuota,
      };

      await customerApi.updateQuota(newCustomerId, quotaData);

      setMessage('客户创建成功！');
      setMessageType('success');
      setShowCreateModal(false);
      setNewCustomer({
        name: '',
        email: '',
        initialBalance: 0,
        creditLimit: 5000,
        vmQuota: 10,
        storageQuota: 1000,
        bandwidthQuota: 1000,
        databaseQuota: 5,
      });
      fetchCustomers();
    } catch (error) {
      setMessage('客户创建失败: ' + (error.response?.data?.error || error.response?.data || error.message));
      setMessageType('error');
    }
  };

  const handleRecharge = async (e) => {
    e.preventDefault();
    if (!selectedCustomer || rechargeAmount <= 0) return;

    try {
      await customerApi.recharge(selectedCustomer.id, rechargeAmount);
      setMessage('充值成功！');
      setMessageType('success');
      setShowRechargeModal(false);
      setRechargeAmount(0);
      setSelectedCustomer(null);
      fetchCustomers();
    } catch (error) {
      setMessage('充值失败: ' + (error.response?.data?.error || error.response?.data || error.message));
      setMessageType('error');
    }
  };

  const handleEditQuota = (customer) => {
    setSelectedCustomer(customer);
    const quota = quotas[customer.id];
    if (quota) {
      setQuotaForm({
        vmQuota: quota.vmQuota,
        storageQuota: quota.storageQuota,
        bandwidthQuota: quota.bandwidthQuota,
        databaseQuota: quota.databaseQuota,
      });
    }
    setShowQuotaModal(true);
  };

  const handleUpdateQuota = async (e) => {
    e.preventDefault();
    if (!selectedCustomer) return;

    try {
      await customerApi.updateQuota(selectedCustomer.id, quotaForm);
      setMessage('配额更新成功！');
      setMessageType('success');
      setShowQuotaModal(false);
      setSelectedCustomer(null);
      fetchCustomers();
    } catch (error) {
      setMessage('配额更新失败: ' + (error.response?.data?.error || error.response?.data || error.message));
      setMessageType('error');
    }
  };

  const handleShowDetail = (customer) => {
    setSelectedCustomer(customer);
    setShowDetailModal(true);
  };

  const getStatusBadge = (status) => {
    let className = 'status-badge ';
    switch (status) {
      case 'ACTIVE':
        className += 'status-active';
        break;
      case 'RESTRICTED':
        className += 'status-restricted';
        break;
      case 'SUSPENDED':
      case 'TERMINATED':
        className += 'status-suspended';
        break;
      default:
        className += 'status-active';
    }
    return <span className={className}>{status}</span>;
  };

  const getUsagePercentage = (used, quota) => {
    if (!quota || quota === 0) return 0;
    return Math.min(100, (used / quota) * 100);
  };

  const getUsageColor = (percentage) => {
    if (percentage >= 90) return '#dc3545';
    if (percentage >= 70) return '#ffc107';
    return '#28a745';
  };

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  return (
    <div>
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h2 style={{ margin: 0, borderBottom: 'none', paddingBottom: 0 }}>客户管理</h2>
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
            + 新增客户
          </button>
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

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(450px, 1fr))', gap: '20px' }}>
          {customers.map(customer => {
            const quota = quotas[customer.id];
            return (
              <div key={customer.id} className="card" style={{ marginBottom: 0 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '15px' }}>
                  <div>
                    <h4 style={{ margin: 0, color: '#333' }}>{customer.name}</h4>
                    <p style={{ margin: 0, fontSize: '12px', color: '#666', marginTop: '3px' }}>
                      {customer.id} · {customer.email}
                    </p>
                  </div>
                  {getStatusBadge(customer.status)}
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginBottom: '15px' }}>
                  <div style={{ padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                    <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>账户余额</p>
                    <p style={{ 
                      margin: 0, 
                      fontWeight: 'bold', 
                      fontSize: '18px',
                      color: '#28a745' 
                    }}>
                      ¥{customer.accountBalance?.toFixed(2) || '0.00'}
                    </p>
                  </div>
                  <div style={{ padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                    <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>已使用信用</p>
                    <p style={{ 
                      margin: 0, 
                      fontWeight: 'bold', 
                      fontSize: '18px',
                      color: customer.creditUsed > 0 ? '#dc3545' : '#28a745' 
                    }}>
                      ¥{customer.creditUsed?.toFixed(2) || '0.00'}
                    </p>
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginBottom: '15px' }}>
                  <div style={{ padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                    <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>信用额度</p>
                    <p style={{ margin: 0, fontWeight: 'bold', fontSize: '18px' }}>
                      ¥{customer.creditLimit?.toFixed(2) || '0.00'}
                    </p>
                  </div>
                  <div style={{ padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                    <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>总可用金额</p>
                    <p style={{ 
                      margin: 0, 
                      fontWeight: 'bold', 
                      fontSize: '18px',
                      color: (customer.accountBalance + (customer.creditLimit || 0) - (customer.creditUsed || 0)) < 0 ? '#dc3545' : '#28a745' 
                    }}>
                      ¥{(customer.accountBalance + (customer.creditLimit || 0) - (customer.creditUsed || 0)).toFixed(2)}
                    </p>
                  </div>
                </div>

                {quota && (
                  <div style={{ marginBottom: '15px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                      <strong style={{ fontSize: '13px' }}>📊 配额使用情况</strong>
                    </div>
                    
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                      <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                          <span>🖥️ 云主机</span>
                          <span>{quota.vmUsed}/{quota.vmQuota}</span>
                        </div>
                        <div style={{ height: '8px', backgroundColor: '#e9ecef', borderRadius: '4px', overflow: 'hidden' }}>
                          <div 
                            style={{ 
                              height: '100%', 
                              width: `${getUsagePercentage(quota.vmUsed, quota.vmQuota)}%`,
                              backgroundColor: getUsageColor(getUsagePercentage(quota.vmUsed, quota.vmQuota)),
                              transition: 'width 0.3s'
                            }}
                          />
                        </div>
                      </div>

                      <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                          <span>💾 存储 (GB)</span>
                          <span>{quota.storageUsed}/{quota.storageQuota}</span>
                        </div>
                        <div style={{ height: '8px', backgroundColor: '#e9ecef', borderRadius: '4px', overflow: 'hidden' }}>
                          <div 
                            style={{ 
                              height: '100%', 
                              width: `${getUsagePercentage(quota.storageUsed, quota.storageQuota)}%`,
                              backgroundColor: getUsageColor(getUsagePercentage(quota.storageUsed, quota.storageQuota)),
                              transition: 'width 0.3s'
                            }}
                          />
                        </div>
                      </div>

                      <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                          <span>🌐 带宽 (Mbps)</span>
                          <span>{quota.bandwidthUsed}/{quota.bandwidthQuota}</span>
                        </div>
                        <div style={{ height: '8px', backgroundColor: '#e9ecef', borderRadius: '4px', overflow: 'hidden' }}>
                          <div 
                            style={{ 
                              height: '100%', 
                              width: `${getUsagePercentage(quota.bandwidthUsed, quota.bandwidthQuota)}%`,
                              backgroundColor: getUsageColor(getUsagePercentage(quota.bandwidthUsed, quota.bandwidthQuota)),
                              transition: 'width 0.3s'
                            }}
                          />
                        </div>
                      </div>

                      <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                          <span>🗄️ 数据库</span>
                          <span>{quota.databaseUsed}/{quota.databaseQuota}</span>
                        </div>
                        <div style={{ height: '8px', backgroundColor: '#e9ecef', borderRadius: '4px', overflow: 'hidden' }}>
                          <div 
                            style={{ 
                              height: '100%', 
                              width: `${getUsagePercentage(quota.databaseUsed, quota.databaseQuota)}%`,
                              backgroundColor: getUsageColor(getUsagePercentage(quota.databaseUsed, quota.databaseQuota)),
                              transition: 'width 0.3s'
                            }}
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                  <button 
                    className="btn btn-secondary"
                    style={{ padding: '6px 12px', fontSize: '13px' }}
                    onClick={() => handleShowDetail(customer)}
                  >
                    👁️ 详情
                  </button>
                  <button 
                    className="btn btn-primary"
                    style={{ padding: '6px 12px', fontSize: '13px' }}
                    onClick={() => handleEditQuota(customer)}
                  >
                    ⚙️ 配额
                  </button>
                  <button 
                    className="btn btn-success"
                    style={{ padding: '6px 12px', fontSize: '13px' }}
                    onClick={() => {
                      setSelectedCustomer(customer);
                      setShowRechargeModal(true);
                    }}
                  >
                    💰 充值
                  </button>
                </div>
              </div>
            );
          })}
        </div>

        {customers.length === 0 && (
          <div style={{ textAlign: 'center', padding: '60px', color: '#666' }}>
            <div style={{ fontSize: '48px', marginBottom: '15px' }}>👥</div>
            <h3 style={{ marginBottom: '10px' }}>暂无客户</h3>
            <p style={{ marginBottom: '20px' }}>点击上方"新增客户"按钮开始创建客户</p>
            <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
              + 创建第一个客户
            </button>
          </div>
        )}
      </div>

      {showCreateModal && (
        <div className="modal" onClick={() => setShowCreateModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxHeight: '85vh', width: '600px' }}>
            <h3>创建新客户</h3>
            <form onSubmit={handleCreateCustomer}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                <div className="form-group">
                  <label>客户名称 *</label>
                  <input
                    type="text"
                    value={newCustomer.name}
                    onChange={e => setNewCustomer({ ...newCustomer, name: e.target.value })}
                    placeholder="请输入客户名称"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>邮箱 *</label>
                  <input
                    type="email"
                    value={newCustomer.email}
                    onChange={e => setNewCustomer({ ...newCustomer, email: e.target.value })}
                    placeholder="请输入邮箱"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>初始余额 (¥)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={newCustomer.initialBalance}
                    onChange={e => setNewCustomer({ ...newCustomer, initialBalance: parseFloat(e.target.value) || 0 })}
                  />
                </div>
                <div className="form-group">
                  <label>信用额度 (¥)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={newCustomer.creditLimit}
                    onChange={e => setNewCustomer({ ...newCustomer, creditLimit: parseFloat(e.target.value) || 0 })}
                  />
                </div>
              </div>

              <h4 style={{ marginTop: '20px', marginBottom: '15px', color: '#667eea' }}>📊 资源配额设置</h4>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                <div className="form-group">
                  <label>🖥️ 云主机配额</label>
                  <input
                    type="number"
                    min="0"
                    value={newCustomer.vmQuota}
                    onChange={e => setNewCustomer({ ...newCustomer, vmQuota: parseInt(e.target.value) || 0 })}
                  />
                </div>
                <div className="form-group">
                  <label>💾 存储配额 (GB)</label>
                  <input
                    type="number"
                    min="0"
                    value={newCustomer.storageQuota}
                    onChange={e => setNewCustomer({ ...newCustomer, storageQuota: parseInt(e.target.value) || 0 })}
                  />
                </div>
                <div className="form-group">
                  <label>🌐 带宽配额 (Mbps)</label>
                  <input
                    type="number"
                    min="0"
                    value={newCustomer.bandwidthQuota}
                    onChange={e => setNewCustomer({ ...newCustomer, bandwidthQuota: parseInt(e.target.value) || 0 })}
                  />
                </div>
                <div className="form-group">
                  <label>🗄️ 数据库配额</label>
                  <input
                    type="number"
                    min="0"
                    value={newCustomer.databaseQuota}
                    onChange={e => setNewCustomer({ ...newCustomer, databaseQuota: parseInt(e.target.value) || 0 })}
                  />
                </div>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>
                  取消
                </button>
                <button type="submit" className="btn btn-primary">
                  创建客户
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showRechargeModal && selectedCustomer && (
        <div className="modal" onClick={() => setShowRechargeModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h3>💰 客户充值</h3>
            
            <div style={{ padding: '15px', backgroundColor: '#f0f9ff', borderRadius: '4px', marginBottom: '20px' }}>
              <p style={{ margin: 0 }}><strong>客户:</strong> {selectedCustomer.name} ({selectedCustomer.id})</p>
              <p style={{ margin: 0, marginTop: '8px' }}>
                <strong>当前余额:</strong> 
                <span style={{ 
                  fontSize: '20px', 
                  fontWeight: 'bold',
                  color: selectedCustomer.accountBalance < 0 ? '#dc3545' : '#28a745',
                  marginLeft: '10px'
                }}>
                  ¥{selectedCustomer.accountBalance?.toFixed(2) || '0.00'}
                </span>
              </p>
            </div>

            <form onSubmit={handleRecharge}>
              <div className="form-group">
                <label>充值金额 (¥) *</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={rechargeAmount}
                  onChange={e => setRechargeAmount(parseFloat(e.target.value) || 0)}
                  placeholder="请输入充值金额"
                  required
                  style={{ fontSize: '18px', padding: '12px' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
                {[100, 500, 1000, 2000, 5000].map(amount => (
                  <button
                    key={amount}
                    type="button"
                    className="btn btn-secondary"
                    style={{ flex: 1 }}
                    onClick={() => setRechargeAmount(amount)}
                  >
                    ¥{amount}
                  </button>
                ))}
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowRechargeModal(false)}>
                  取消
                </button>
                <button type="submit" className="btn btn-success">
                  确认充值
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showQuotaModal && selectedCustomer && (
        <div className="modal" onClick={() => setShowQuotaModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h3>⚙️ 编辑资源配额</h3>
            
            <div style={{ padding: '12px', backgroundColor: '#fff3cd', borderRadius: '4px', marginBottom: '20px' }}>
              <p style={{ margin: 0, fontSize: '13px' }}>
                ⚠️ <strong>注意:</strong> 缩减配额不能低于当前已使用容量，否则会更新失败。
              </p>
            </div>

            <div style={{ padding: '12px', backgroundColor: '#f8f9fa', borderRadius: '4px', marginBottom: '20px' }}>
              <p style={{ margin: 0 }}><strong>客户:</strong> {selectedCustomer.name} ({selectedCustomer.id})</p>
            </div>

            <form onSubmit={handleUpdateQuota}>
              <div className="form-group">
                <label>🖥️ 云主机配额</label>
                <input
                  type="number"
                  min="0"
                  value={quotaForm.vmQuota}
                  onChange={e => setQuotaForm({ ...quotaForm, vmQuota: parseInt(e.target.value) || 0 })}
                />
                <p style={{ marginTop: '5px', fontSize: '12px', color: '#666' }}>
                  当前已使用: {quotas[selectedCustomer.id]?.vmUsed || 0} 台
                </p>
              </div>

              <div className="form-group">
                <label>💾 存储配额 (GB)</label>
                <input
                  type="number"
                  min="0"
                  value={quotaForm.storageQuota}
                  onChange={e => setQuotaForm({ ...quotaForm, storageQuota: parseInt(e.target.value) || 0 })}
                />
                <p style={{ marginTop: '5px', fontSize: '12px', color: '#666' }}>
                  当前已使用: {quotas[selectedCustomer.id]?.storageUsed || 0} GB
                </p>
              </div>

              <div className="form-group">
                <label>🌐 带宽配额 (Mbps)</label>
                <input
                  type="number"
                  min="0"
                  value={quotaForm.bandwidthQuota}
                  onChange={e => setQuotaForm({ ...quotaForm, bandwidthQuota: parseInt(e.target.value) || 0 })}
                />
                <p style={{ marginTop: '5px', fontSize: '12px', color: '#666' }}>
                  当前已使用: {quotas[selectedCustomer.id]?.bandwidthUsed || 0} Mbps
                </p>
              </div>

              <div className="form-group">
                <label>🗄️ 数据库配额</label>
                <input
                  type="number"
                  min="0"
                  value={quotaForm.databaseQuota}
                  onChange={e => setQuotaForm({ ...quotaForm, databaseQuota: parseInt(e.target.value) || 0 })}
                />
                <p style={{ marginTop: '5px', fontSize: '12px', color: '#666' }}>
                  当前已使用: {quotas[selectedCustomer.id]?.databaseUsed || 0} 个
                </p>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowQuotaModal(false)}>
                  取消
                </button>
                <button type="submit" className="btn btn-primary">
                  保存修改
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showDetailModal && selectedCustomer && (
        <div className="modal" onClick={() => setShowDetailModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()} style={{ width: '500px' }}>
            <h3>👤 客户详情</h3>
            
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
              <div style={{ padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>客户ID</p>
                <p style={{ margin: 0, fontWeight: 'bold' }}>{selectedCustomer.id}</p>
              </div>
              <div style={{ padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>客户名称</p>
                <p style={{ margin: 0, fontWeight: 'bold' }}>{selectedCustomer.name}</p>
              </div>
              <div style={{ padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>邮箱</p>
                <p style={{ margin: 0, fontWeight: 'bold' }}>{selectedCustomer.email}</p>
              </div>
              <div style={{ padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>状态</p>
                <div style={{ marginTop: '4px' }}>{getStatusBadge(selectedCustomer.status)}</div>
              </div>
              <div style={{ padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>账户余额</p>
                <p style={{ 
                  margin: 0, 
                  fontWeight: 'bold',
                  color: selectedCustomer.accountBalance < 0 ? '#dc3545' : '#28a745'
                }}>
                  ¥{selectedCustomer.accountBalance?.toFixed(2) || '0.00'}
                </p>
              </div>
              <div style={{ padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>信用额度</p>
                <p style={{ margin: 0, fontWeight: 'bold' }}>
                  ¥{selectedCustomer.creditLimit?.toFixed(2) || '0.00'}
                </p>
              </div>
            </div>

            {quotas[selectedCustomer.id] && (
              <>
                <h4 style={{ marginTop: '20px', marginBottom: '15px' }}>📊 配额详情</h4>
                <table style={{ fontSize: '14px' }}>
                  <thead>
                    <tr>
                      <th>资源类型</th>
                      <th>已使用</th>
                      <th>配额</th>
                      <th>使用率</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>🖥️ 云主机</td>
                      <td>{quotas[selectedCustomer.id].vmUsed}</td>
                      <td>{quotas[selectedCustomer.id].vmQuota}</td>
                      <td>{getUsagePercentage(quotas[selectedCustomer.id].vmUsed, quotas[selectedCustomer.id].vmQuota).toFixed(1)}%</td>
                    </tr>
                    <tr>
                      <td>💾 存储</td>
                      <td>{quotas[selectedCustomer.id].storageUsed} GB</td>
                      <td>{quotas[selectedCustomer.id].storageQuota} GB</td>
                      <td>{getUsagePercentage(quotas[selectedCustomer.id].storageUsed, quotas[selectedCustomer.id].storageQuota).toFixed(1)}%</td>
                    </tr>
                    <tr>
                      <td>🌐 带宽</td>
                      <td>{quotas[selectedCustomer.id].bandwidthUsed} Mbps</td>
                      <td>{quotas[selectedCustomer.id].bandwidthQuota} Mbps</td>
                      <td>{getUsagePercentage(quotas[selectedCustomer.id].bandwidthUsed, quotas[selectedCustomer.id].bandwidthQuota).toFixed(1)}%</td>
                    </tr>
                    <tr>
                      <td>🗄️ 数据库</td>
                      <td>{quotas[selectedCustomer.id].databaseUsed}</td>
                      <td>{quotas[selectedCustomer.id].databaseQuota}</td>
                      <td>{getUsagePercentage(quotas[selectedCustomer.id].databaseUsed, quotas[selectedCustomer.id].databaseQuota).toFixed(1)}%</td>
                    </tr>
                  </tbody>
                </table>
              </>
            )}

            <div className="modal-actions">
              <button type="button" className="btn btn-secondary" onClick={() => setShowDetailModal(false)}>
                关闭
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Customers;
