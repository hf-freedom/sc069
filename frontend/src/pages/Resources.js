import React, { useState, useEffect } from 'react';
import { resourceApi, customerApi } from '../services/api';

function Resources() {
  const [resources, setResources] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedCustomer, setSelectedCustomer] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [createType, setCreateType] = useState('vm');
  const [editingResource, setEditingResource] = useState(null);
  const [message, setMessage] = useState(null);
  const [messageType, setMessageType] = useState('success');

  const [vmForm, setVmForm] = useState({
    customerId: '',
    name: '',
    cpuCores: 2,
    memoryGB: 4,
  });

  const [storageForm, setStorageForm] = useState({
    customerId: '',
    name: '',
    storageGB: 100,
  });

  const [bandwidthForm, setBandwidthForm] = useState({
    customerId: '',
    name: '',
    bandwidthMbps: 100,
  });

  const [dbForm, setDbForm] = useState({
    customerId: '',
    name: '',
    dbType: 'mysql',
    dbVersion: 8,
    storageGB: 50,
  });

  const [editForm, setEditForm] = useState({
    name: '',
    cpuCores: 0,
    memoryGB: 0,
    storageGB: 0,
    bandwidthMbps: 0,
    minCapacity: 0,
    maxCapacity: 0,
  });

  useEffect(() => {
    fetchData();
  }, [selectedCustomer]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [customersRes, resourcesRes] = await Promise.all([
        customerApi.getAll(),
        resourceApi.getAll(selectedCustomer || null),
      ]);
      setCustomers(customersRes.data);
      setResources(resourcesRes.data);
    } catch (error) {
      console.error('获取数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateResource = async (e) => {
    e.preventDefault();
    try {
      let response;
      switch (createType) {
        case 'vm':
          response = await resourceApi.createVm(vmForm);
          break;
        case 'storage':
          response = await resourceApi.createStorage(storageForm);
          break;
        case 'bandwidth':
          response = await resourceApi.createBandwidth(bandwidthForm);
          break;
        case 'database':
          response = await resourceApi.createDatabase(dbForm);
          break;
      }

      setMessage('资源创建成功！');
      setMessageType('success');
      setShowCreateModal(false);
      resetForms();
      fetchData();
    } catch (error) {
      setMessage('资源创建失败: ' + (error.response?.data?.error || error.response?.data || error.message));
      setMessageType('error');
    }
  };

  const handleEditResource = (resource) => {
    setEditingResource(resource);
    setEditForm({
      name: resource.name || '',
      cpuCores: resource.cpuCores || 0,
      memoryGB: resource.memoryGB || 0,
      storageGB: resource.storageGB || 0,
      bandwidthMbps: resource.bandwidthMbps || 0,
      minCapacity: resource.minCapacity || 0,
      maxCapacity: resource.maxCapacity || 0,
    });
    setShowEditModal(true);
  };

  const handleUpdateResource = async (e) => {
    e.preventDefault();
    if (!editingResource) return;

    try {
      const updateData = {};
      
      if (editForm.name && editForm.name !== editingResource.name) {
        updateData.name = editForm.name;
      }

      if (editingResource.type === 'VM') {
        if (editForm.cpuCores !== editingResource.cpuCores) {
          updateData.cpuCores = editForm.cpuCores;
        }
        if (editForm.memoryGB !== editingResource.memoryGB) {
          updateData.memoryGB = editForm.memoryGB;
        }
      } else if (editingResource.type === 'STORAGE') {
        if (editForm.storageGB !== editingResource.storageGB) {
          updateData.storageGB = editForm.storageGB;
        }
      } else if (editingResource.type === 'BANDWIDTH') {
        if (editForm.bandwidthMbps !== editingResource.bandwidthMbps) {
          updateData.bandwidthMbps = editForm.bandwidthMbps;
        }
      } else if (editingResource.type === 'DATABASE') {
        if (editForm.storageGB !== editingResource.storageGB) {
          updateData.storageGB = editForm.storageGB;
        }
      }

      if (editForm.minCapacity !== editingResource.minCapacity) {
        updateData.minCapacity = editForm.minCapacity;
      }
      if (editForm.maxCapacity !== editingResource.maxCapacity) {
        updateData.maxCapacity = editForm.maxCapacity;
      }

      if (Object.keys(updateData).length === 0) {
        setMessage('没有需要更新的字段');
        setMessageType('error');
        return;
      }

      await resourceApi.update(editingResource.id, updateData);
      setMessage('资源更新成功！');
      setMessageType('success');
      setShowEditModal(false);
      setEditingResource(null);
      fetchData();
    } catch (error) {
      setMessage('资源更新失败: ' + (error.response?.data?.error || error.response?.data || error.message));
      setMessageType('error');
    }
  };

  const resetForms = () => {
    setVmForm({ customerId: '', name: '', cpuCores: 2, memoryGB: 4 });
    setStorageForm({ customerId: '', name: '', storageGB: 100 });
    setBandwidthForm({ customerId: '', name: '', bandwidthMbps: 100 });
    setDbForm({ customerId: '', name: '', dbType: 'mysql', dbVersion: 8, storageGB: 50 });
  };

  const handleStartResource = async (resourceId) => {
    try {
      await resourceApi.start(resourceId);
      setMessage('资源启动成功！');
      setMessageType('success');
      fetchData();
    } catch (error) {
      setMessage('操作失败: ' + (error.response?.data || error.message));
      setMessageType('error');
    }
  };

  const handleStopResource = async (resourceId) => {
    try {
      await resourceApi.stop(resourceId);
      setMessage('资源停止成功！');
      setMessageType('success');
      fetchData();
    } catch (error) {
      setMessage('操作失败: ' + (error.response?.data || error.message));
      setMessageType('error');
    }
  };

  const handleReleaseResource = async (resourceId) => {
    if (window.confirm('确定要释放该资源吗？此操作不可撤销。')) {
      try {
        await resourceApi.release(resourceId);
        setMessage('资源释放成功！');
        setMessageType('success');
        fetchData();
      } catch (error) {
        setMessage('操作失败: ' + (error.response?.data || error.message));
        setMessageType('error');
      }
    }
  };

  const getStatusBadge = (status) => {
    let className = 'status-badge ';
    switch (status) {
      case 'RUNNING':
        className += 'status-running';
        break;
      case 'STOPPED':
        className += 'status-stopped';
        break;
      case 'PENDING':
        className += 'status-pending';
        break;
      case 'RESTRICTED':
        className += 'status-restricted';
        break;
      case 'TERMINATED':
        className += 'status-terminated';
        break;
      default:
        className += 'status-stopped';
    }
    return <span className={className}>{status}</span>;
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

  const getTypeIcon = (type) => {
    switch (type) {
      case 'VM':
        return '🖥️';
      case 'STORAGE':
        return '💾';
      case 'BANDWIDTH':
        return '🌐';
      case 'DATABASE':
        return '🗄️';
      default:
        return '📦';
    }
  };

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  return (
    <div>
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <h2 style={{ margin: 0, borderBottom: 'none', paddingBottom: 0 }}>资源管理</h2>
            <select
              value={selectedCustomer}
              onChange={e => setSelectedCustomer(e.target.value)}
              style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
            >
              <option value="">全部客户</option>
              {customers.map(c => (
                <option key={c.id} value={c.id}>{c.name} ({c.id})</option>
              ))}
            </select>
          </div>
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
            + 创建资源
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

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '20px' }}>
          {resources.map(resource => (
            <div 
              key={resource.id} 
              className="card" 
              style={{ marginBottom: 0, transition: 'box-shadow 0.3s' }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '15px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <span style={{ fontSize: '24px' }}>{getTypeIcon(resource.type)}</span>
                  <div>
                    <h4 style={{ margin: 0, color: '#333' }}>{resource.name}</h4>
                    <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>
                      {getTypeLabel(resource.type)} · {resource.id}
                    </p>
                  </div>
                </div>
                {getStatusBadge(resource.status)}
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginBottom: '15px' }}>
                <div style={{ padding: '8px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                  <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>客户ID</p>
                  <p style={{ margin: 0, fontWeight: 'bold' }}>{resource.customerId}</p>
                </div>
                <div style={{ padding: '8px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                  <p style={{ margin: 0, fontSize: '12px', color: '#666' }}>每小时费用</p>
                  <p style={{ margin: 0, fontWeight: 'bold', color: '#dc3545' }}>
                    ¥{resource.hourlyRate?.toFixed(2) || '0.00'}
                  </p>
                </div>
              </div>

              <div style={{ padding: '10px', backgroundColor: '#f0f4ff', borderRadius: '4px', marginBottom: '15px' }}>
                <p style={{ margin: 0, fontSize: '12px', color: '#666', marginBottom: '5px' }}>
                  <strong>规格:</strong>
                </p>
                <p style={{ margin: 0 }}>
                  {resource.type === 'VM' && `${resource.cpuCores} 核 CPU · ${resource.memoryGB} GB 内存`}
                  {resource.type === 'STORAGE' && `${resource.storageGB} GB 存储`}
                  {resource.type === 'BANDWIDTH' && `${resource.bandwidthMbps} Mbps 带宽`}
                  {resource.type === 'DATABASE' && `${resource.dbType?.toUpperCase()} · ${resource.storageGB} GB 存储`}
                </p>
                <p style={{ margin: 0, marginTop: '5px', fontSize: '12px', color: '#666' }}>
                  容量范围: {resource.minCapacity} - {resource.maxCapacity} (当前: {resource.currentCapacity})
                </p>
              </div>

              <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                {resource.status === 'STOPPED' && (
                  <button 
                    className="btn btn-success" 
                    style={{ padding: '6px 12px', fontSize: '13px' }}
                    onClick={() => handleStartResource(resource.id)}
                  >
                    ▶ 启动
                  </button>
                )}
                {resource.status === 'RUNNING' && (
                  <button 
                    className="btn btn-warning" 
                    style={{ padding: '6px 12px', fontSize: '13px' }}
                    onClick={() => handleStopResource(resource.id)}
                  >
                    ⏸ 停止
                  </button>
                )}
                {resource.status !== 'TERMINATED' && (
                  <>
                    <button 
                      className="btn btn-primary" 
                      style={{ padding: '6px 12px', fontSize: '13px' }}
                      onClick={() => handleEditResource(resource)}
                    >
                      ✏️ 编辑
                    </button>
                    <button 
                      className="btn btn-danger" 
                      style={{ padding: '6px 12px', fontSize: '13px' }}
                      onClick={() => handleReleaseResource(resource.id)}
                    >
                      🗑️ 释放
                    </button>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>

        {resources.length === 0 && (
          <div style={{ textAlign: 'center', padding: '60px', color: '#666' }}>
            <div style={{ fontSize: '48px', marginBottom: '15px' }}>📦</div>
            <h3 style={{ marginBottom: '10px' }}>暂无资源</h3>
            <p style={{ marginBottom: '20px' }}>点击上方"创建资源"按钮开始创建您的云资源</p>
            <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
              + 创建第一个资源
            </button>
          </div>
        )}
      </div>

      {showCreateModal && (
        <div className="modal" onClick={() => setShowCreateModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxHeight: '85vh' }}>
            <h3>创建云资源</h3>
            
            <div className="tabs">
              <div 
                className={`tab ${createType === 'vm' ? 'active' : ''}`}
                onClick={() => setCreateType('vm')}
              >🖥️ 云主机</div>
              <div 
                className={`tab ${createType === 'storage' ? 'active' : ''}`}
                onClick={() => setCreateType('storage')}
              >💾 存储</div>
              <div 
                className={`tab ${createType === 'bandwidth' ? 'active' : ''}`}
                onClick={() => setCreateType('bandwidth')}
              >🌐 带宽</div>
              <div 
                className={`tab ${createType === 'database' ? 'active' : ''}`}
                onClick={() => setCreateType('database')}
              >🗄️ 数据库</div>
            </div>

            <form onSubmit={handleCreateResource}>
              {createType === 'vm' && (
                <>
                  <div className="form-group">
                    <label>选择客户 *</label>
                    <select
                      value={vmForm.customerId}
                      onChange={e => setVmForm({ ...vmForm, customerId: e.target.value })}
                      required
                    >
                      <option value="">请选择客户</option>
                      {customers.map(c => (
                        <option key={c.id} value={c.id}>{c.name} ({c.id}) - 余额: ¥{c.accountBalance?.toFixed(2)}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>资源名称 *</label>
                    <input
                      type="text"
                      value={vmForm.name}
                      onChange={e => setVmForm({ ...vmForm, name: e.target.value })}
                      placeholder="例如: web-server-01"
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>CPU 核数</label>
                    <select
                      value={vmForm.cpuCores}
                      onChange={e => setVmForm({ ...vmForm, cpuCores: parseInt(e.target.value) })}
                    >
                      <option value="1">1 核</option>
                      <option value="2">2 核</option>
                      <option value="4">4 核</option>
                      <option value="8">8 核</option>
                      <option value="16">16 核</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>内存 (GB)</label>
                    <select
                      value={vmForm.memoryGB}
                      onChange={e => setVmForm({ ...vmForm, memoryGB: parseInt(e.target.value) })}
                    >
                      <option value="1">1 GB</option>
                      <option value="2">2 GB</option>
                      <option value="4">4 GB</option>
                      <option value="8">8 GB</option>
                      <option value="16">16 GB</option>
                      <option value="32">32 GB</option>
                    </select>
                  </div>
                  <div style={{ padding: '12px', backgroundColor: '#f0f9ff', borderRadius: '4px', border: '1px solid #b3d9ff' }}>
                    <p style={{ margin: 0 }}>
                      <strong>💰 预估每小时费用:</strong> 
                      <span style={{ color: '#dc3545', fontSize: '18px', marginLeft: '8px' }}>
                        ¥{(vmForm.cpuCores * 0.5 + vmForm.memoryGB * 0.2).toFixed(2)}
                      </span>
                    </p>
                    <p style={{ margin: 0, marginTop: '5px', fontSize: '12px', color: '#666' }}>
                      CPU: ¥0.5/核/小时 · 内存: ¥0.2/GB/小时
                    </p>
                  </div>
                </>
              )}

              {createType === 'storage' && (
                <>
                  <div className="form-group">
                    <label>选择客户 *</label>
                    <select
                      value={storageForm.customerId}
                      onChange={e => setStorageForm({ ...storageForm, customerId: e.target.value })}
                      required
                    >
                      <option value="">请选择客户</option>
                      {customers.map(c => (
                        <option key={c.id} value={c.id}>{c.name} ({c.id}) - 余额: ¥{c.accountBalance?.toFixed(2)}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>资源名称 *</label>
                    <input
                      type="text"
                      value={storageForm.name}
                      onChange={e => setStorageForm({ ...storageForm, name: e.target.value })}
                      placeholder="例如: data-storage-01"
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>存储容量 (GB)</label>
                    <input
                      type="number"
                      min="10"
                      step="10"
                      value={storageForm.storageGB}
                      onChange={e => setStorageForm({ ...storageForm, storageGB: parseInt(e.target.value) || 100 })}
                    />
                    <p style={{ marginTop: '5px', fontSize: '12px', color: '#666' }}>
                      建议: 10GB ~ 1000GB
                    </p>
                  </div>
                  <div style={{ padding: '12px', backgroundColor: '#f0f9ff', borderRadius: '4px', border: '1px solid #b3d9ff' }}>
                    <p style={{ margin: 0 }}>
                      <strong>💰 预估每小时费用:</strong> 
                      <span style={{ color: '#dc3545', fontSize: '18px', marginLeft: '8px' }}>
                        ¥{(storageForm.storageGB * 0.01).toFixed(2)}
                      </span>
                    </p>
                    <p style={{ margin: 0, marginTop: '5px', fontSize: '12px', color: '#666' }}>
                      存储: ¥0.01/GB/小时
                    </p>
                  </div>
                </>
              )}

              {createType === 'bandwidth' && (
                <>
                  <div className="form-group">
                    <label>选择客户 *</label>
                    <select
                      value={bandwidthForm.customerId}
                      onChange={e => setBandwidthForm({ ...bandwidthForm, customerId: e.target.value })}
                      required
                    >
                      <option value="">请选择客户</option>
                      {customers.map(c => (
                        <option key={c.id} value={c.id}>{c.name} ({c.id}) - 余额: ¥{c.accountBalance?.toFixed(2)}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>资源名称 *</label>
                    <input
                      type="text"
                      value={bandwidthForm.name}
                      onChange={e => setBandwidthForm({ ...bandwidthForm, name: e.target.value })}
                      placeholder="例如: bandwidth-01"
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>带宽 (Mbps)</label>
                    <select
                      value={bandwidthForm.bandwidthMbps}
                      onChange={e => setBandwidthForm({ ...bandwidthForm, bandwidthMbps: parseInt(e.target.value) })}
                    >
                      <option value="10">10 Mbps</option>
                      <option value="50">50 Mbps</option>
                      <option value="100">100 Mbps</option>
                      <option value="200">200 Mbps</option>
                      <option value="500">500 Mbps</option>
                      <option value="1000">1000 Mbps (1G)</option>
                    </select>
                  </div>
                  <div style={{ padding: '12px', backgroundColor: '#f0f9ff', borderRadius: '4px', border: '1px solid #b3d9ff' }}>
                    <p style={{ margin: 0 }}>
                      <strong>💰 预估每小时费用:</strong> 
                      <span style={{ color: '#dc3545', fontSize: '18px', marginLeft: '8px' }}>
                        ¥{(bandwidthForm.bandwidthMbps * 0.1).toFixed(2)}
                      </span>
                    </p>
                    <p style={{ margin: 0, marginTop: '5px', fontSize: '12px', color: '#666' }}>
                      带宽: ¥0.1/Mbps/小时
                    </p>
                  </div>
                </>
              )}

              {createType === 'database' && (
                <>
                  <div className="form-group">
                    <label>选择客户 *</label>
                    <select
                      value={dbForm.customerId}
                      onChange={e => setDbForm({ ...dbForm, customerId: e.target.value })}
                      required
                    >
                      <option value="">请选择客户</option>
                      {customers.map(c => (
                        <option key={c.id} value={c.id}>{c.name} ({c.id}) - 余额: ¥{c.accountBalance?.toFixed(2)}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>资源名称 *</label>
                    <input
                      type="text"
                      value={dbForm.name}
                      onChange={e => setDbForm({ ...dbForm, name: e.target.value })}
                      placeholder="例如: mysql-db-01"
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>数据库类型</label>
                    <select
                      value={dbForm.dbType}
                      onChange={e => setDbForm({ ...dbForm, dbType: e.target.value })}
                    >
                      <option value="mysql">MySQL (较便宜)</option>
                      <option value="postgresql">PostgreSQL</option>
                      <option value="mongodb">MongoDB</option>
                      <option value="redis">Redis</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>存储容量 (GB)</label>
                    <input
                      type="number"
                      min="10"
                      step="10"
                      value={dbForm.storageGB}
                      onChange={e => setDbForm({ ...dbForm, storageGB: parseInt(e.target.value) || 50 })}
                    />
                  </div>
                  <div style={{ padding: '12px', backgroundColor: '#f0f9ff', borderRadius: '4px', border: '1px solid #b3d9ff' }}>
                    <p style={{ margin: 0 }}>
                      <strong>💰 预估每小时费用:</strong> 
                      <span style={{ color: '#dc3545', fontSize: '18px', marginLeft: '8px' }}>
                        ¥{(dbForm.dbType === 'mysql' ? 0.3 + dbForm.storageGB * 0.02 : 0.5 + dbForm.storageGB * 0.02).toFixed(2)}
                      </span>
                    </p>
                    <p style={{ margin: 0, marginTop: '5px', fontSize: '12px', color: '#666' }}>
                      MySQL: ¥0.3/小时起 · 其他: ¥0.5/小时起 · 存储: ¥0.02/GB/小时
                    </p>
                  </div>
                </>
              )}

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>
                  取消
                </button>
                <button type="submit" className="btn btn-primary">
                  创建资源
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showEditModal && editingResource && (
        <div className="modal" onClick={() => setShowEditModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxHeight: '85vh' }}>
            <h3>编辑资源</h3>
            
            <div style={{ padding: '12px', backgroundColor: '#f8f9fa', borderRadius: '4px', marginBottom: '20px' }}>
              <p style={{ margin: 0 }}>
                <strong>{getTypeIcon(editingResource.type)} {editingResource.name}</strong>
              </p>
              <p style={{ margin: 0, marginTop: '5px', fontSize: '13px', color: '#666' }}>
                {getTypeLabel(editingResource.type)} · {editingResource.id}
              </p>
            </div>

            <form onSubmit={handleUpdateResource}>
              <div className="form-group">
                <label>资源名称</label>
                <input
                  type="text"
                  value={editForm.name}
                  onChange={e => setEditForm({ ...editForm, name: e.target.value })}
                />
              </div>

              {editingResource.type === 'VM' && (
                <>
                  <div className="form-group">
                    <label>CPU 核数</label>
                    <select
                      value={editForm.cpuCores}
                      onChange={e => setEditForm({ ...editForm, cpuCores: parseInt(e.target.value) })}
                    >
                      <option value="1">1 核</option>
                      <option value="2">2 核</option>
                      <option value="4">4 核</option>
                      <option value="8">8 核</option>
                      <option value="16">16 核</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>内存 (GB)</label>
                    <select
                      value={editForm.memoryGB}
                      onChange={e => setEditForm({ ...editForm, memoryGB: parseInt(e.target.value) })}
                    >
                      <option value="1">1 GB</option>
                      <option value="2">2 GB</option>
                      <option value="4">4 GB</option>
                      <option value="8">8 GB</option>
                      <option value="16">16 GB</option>
                      <option value="32">32 GB</option>
                    </select>
                  </div>
                </>
              )}

              {(editingResource.type === 'STORAGE' || editingResource.type === 'DATABASE') && (
                <div className="form-group">
                  <label>存储容量 (GB)</label>
                  <input
                    type="number"
                    min={editingResource.minCapacity}
                    value={editForm.storageGB}
                    onChange={e => setEditForm({ ...editForm, storageGB: parseInt(e.target.value) || 0 })}
                  />
                  <p style={{ marginTop: '5px', fontSize: '12px', color: '#666' }}>
                    最小容量: {editingResource.minCapacity} GB (扩容时会检查配额)
                  </p>
                </div>
              )}

              {editingResource.type === 'BANDWIDTH' && (
                <div className="form-group">
                  <label>带宽 (Mbps)</label>
                  <input
                    type="number"
                    min={editingResource.minCapacity}
                    value={editForm.bandwidthMbps}
                    onChange={e => setEditForm({ ...editForm, bandwidthMbps: parseInt(e.target.value) || 0 })}
                  />
                  <p style={{ marginTop: '5px', fontSize: '12px', color: '#666' }}>
                    最小带宽: {editingResource.minCapacity} Mbps (扩容时会检查配额)
                  </p>
                </div>
              )}

              <div className="form-group">
                <label>最小容量</label>
                <input
                  type="number"
                  value={editForm.minCapacity}
                  onChange={e => setEditForm({ ...editForm, minCapacity: parseInt(e.target.value) || 0 })}
                />
              </div>
              <div className="form-group">
                <label>最大容量</label>
                <input
                  type="number"
                  value={editForm.maxCapacity}
                  onChange={e => setEditForm({ ...editForm, maxCapacity: parseInt(e.target.value) || 0 })}
                />
              </div>

              <div style={{ padding: '12px', backgroundColor: '#fff3cd', borderRadius: '4px', marginBottom: '20px' }}>
                <p style={{ margin: 0, fontSize: '13px' }}>
                  ⚠️ <strong>注意:</strong> 扩容会检查客户配额和余额，缩容不能低于当前已使用容量。
                </p>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowEditModal(false)}>
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
    </div>
  );
}

export default Resources;
