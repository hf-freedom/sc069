import React, { useState, useEffect } from 'react';
import { resourceApi, monitoringApi, customerApi } from '../services/api';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Line } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

function Monitoring() {
  const [resources, setResources] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [selectedResource, setSelectedResource] = useState(null);
  const [selectedCustomer, setSelectedCustomer] = useState('');
  const [monitoringData, setMonitoringData] = useState([]);
  const [loading, setLoading] = useState(true);

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
      setResources(resourcesRes.data.filter(r => r.status === 'RUNNING' || r.status === 'RESTRICTED'));
    } catch (error) {
      console.error('获取数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedResource) {
      fetchMonitoringData();
      const interval = setInterval(fetchMonitoringData, 10000);
      return () => clearInterval(interval);
    }
  }, [selectedResource]);

  const fetchMonitoringData = async () => {
    if (!selectedResource) return;
    try {
      const response = await monitoringApi.getLatest(selectedResource.id, 20);
      setMonitoringData(response.data);
    } catch (error) {
      console.error('获取监控数据失败:', error);
    }
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        max: 100,
        title: {
          display: true,
          text: '使用率 (%)',
        },
      },
      x: {
        title: {
          display: true,
          text: '时间',
        },
      },
    },
  };

  const getChartData = (field, label, color) => {
    const sortedData = [...monitoringData].sort((a, b) => 
      new Date(a.timestamp) - new Date(b.timestamp)
    );
    
    return {
      labels: sortedData.map(d => {
        const time = new Date(d.timestamp);
        return time.toLocaleTimeString('zh-CN');
      }),
      datasets: [
        {
          label,
          data: sortedData.map(d => d[field]),
          borderColor: color,
          backgroundColor: color + '20',
          tension: 0.3,
          fill: true,
        },
      ],
    };
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

  if (loading) {
    return <div className="loading">加载中...</div>;
  }

  return (
    <div>
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <h2 style={{ margin: 0, borderBottom: 'none', paddingBottom: 0 }}>监控中心</h2>
            <select
              value={selectedCustomer}
              onChange={e => {
                setSelectedCustomer(e.target.value);
                setSelectedResource(null);
              }}
              style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
            >
              <option value="">全部客户</option>
              {customers.map(c => (
                <option key={c.id} value={c.id}>{c.name} ({c.id})</option>
              ))}
            </select>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: '20px' }}>
          <div>
            <h3 style={{ marginBottom: '15px' }}>运行中资源</h3>
            {resources.length === 0 ? (
              <p style={{ color: '#666' }}>暂无运行中资源</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {resources.map(resource => (
                  <div
                    key={resource.id}
                    style={{
                      padding: '15px',
                      borderRadius: '4px',
                      border: selectedResource?.id === resource.id 
                        ? '2px solid #667eea' 
                        : '1px solid #ddd',
                      cursor: 'pointer',
                      backgroundColor: selectedResource?.id === resource.id ? '#f0f4ff' : 'white',
                    }}
                    onClick={() => setSelectedResource(resource)}
                  >
                    <div style={{ fontWeight: 'bold', marginBottom: '5px' }}>
                      {resource.name}
                    </div>
                    <div style={{ fontSize: '12px', color: '#666' }}>
                      {getTypeLabel(resource.type)} | {resource.id}
                    </div>
                    <div style={{ fontSize: '12px', color: '#666', marginTop: '5px' }}>
                      ¥{resource.hourlyRate?.toFixed(2)}/小时
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div>
            {selectedResource ? (
              <div>
                <h3 style={{ marginBottom: '20px' }}>
                  {selectedResource.name} - 实时监控
                </h3>

                <div className="stats-grid">
                  <div className="stat-card">
                    <div className="stat-value">
                      {monitoringData.length > 0 
                        ? monitoringData[monitoringData.length - 1].cpuUsage?.toFixed(1) + '%' 
                        : '-'}
                    </div>
                    <div className="stat-label">当前 CPU 使用率</div>
                  </div>
                  <div className="stat-card">
                    <div className="stat-value">
                      {monitoringData.length > 0 
                        ? monitoringData[monitoringData.length - 1].memoryUsage?.toFixed(1) + '%' 
                        : '-'}
                    </div>
                    <div className="stat-label">当前内存使用率</div>
                  </div>
                  <div className="stat-card">
                    <div className="stat-value">
                      {monitoringData.length > 0 
                        ? monitoringData[monitoringData.length - 1].diskUsage?.toFixed(1) + '%' 
                        : '-'}
                    </div>
                    <div className="stat-label">当前磁盘使用率</div>
                  </div>
                  <div className="stat-card">
                    <div className="stat-value">
                      {monitoringData.length > 0 
                        ? ((monitoringData[monitoringData.length - 1].networkIn || 0) + 
                          (monitoringData[monitoringData.length - 1].networkOut || 0)).toFixed(1)
                        : '-'}
                    </div>
                    <div className="stat-label">当前网络流量 (MB/s)</div>
                  </div>
                </div>

                {monitoringData.length > 0 && (
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                    <div className="card">
                      <h4>CPU 使用率</h4>
                      <div className="chart-container">
                        <Line options={chartOptions} data={getChartData('cpuUsage', 'CPU 使用率', '#667eea')} />
                      </div>
                    </div>
                    <div className="card">
                      <h4>内存使用率</h4>
                      <div className="chart-container">
                        <Line options={chartOptions} data={getChartData('memoryUsage', '内存使用率', '#28a745')} />
                      </div>
                    </div>
                    <div className="card">
                      <h4>磁盘使用率</h4>
                      <div className="chart-container">
                        <Line options={chartOptions} data={getChartData('diskUsage', '磁盘使用率', '#ffc107')} />
                      </div>
                    </div>
                  </div>
                )}

                {monitoringData.length === 0 && (
                  <div className="card">
                    <div className="loading">暂无监控数据，系统正在收集...</div>
                  </div>
                )}
              </div>
            ) : (
              <div style={{ 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center', 
                height: '400px',
                color: '#666',
              }}>
                请选择一个资源查看监控数据
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Monitoring;
