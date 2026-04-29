import axios from 'axios';

const API_BASE_URL = 'http://localhost:8002/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const customerApi = {
  getAll: () => api.get('/customers'),
  getById: (id) => api.get(`/customers/${id}`),
  create: (data) => api.post('/customers', data),
  getQuota: (id) => api.get(`/customers/${id}/quota`),
  updateQuota: (id, data) => api.put(`/customers/${id}/quota`, data),
  recharge: (id, amount) => api.post(`/customers/${id}/recharge`, { amount }),
  getBalance: (id) => api.get(`/customers/${id}/balance`),
};

export const resourceApi = {
  getAll: (customerId) => {
    const params = customerId ? { params: { customerId } } : {};
    return api.get('/resources', params);
  },
  getById: (id) => api.get(`/resources/${id}`),
  createVm: (data) => api.post('/resources/vm', data),
  createStorage: (data) => api.post('/resources/storage', data),
  createBandwidth: (data) => api.post('/resources/bandwidth', data),
  createDatabase: (data) => api.post('/resources/database', data),
  start: (id) => api.post(`/resources/${id}/start`),
  stop: (id) => api.post(`/resources/${id}/stop`),
  release: (id) => api.delete(`/resources/${id}`),
  update: (id, data) => api.put(`/resources/${id}`, data),
};

export const monitoringApi = {
  getLatest: (resourceId, limit = 10) => 
    api.get(`/monitoring/resources/${resourceId}/latest`, { params: { limit } }),
  getByTimeRange: (resourceId, startTime, endTime) => 
    api.get(`/monitoring/resources/${resourceId}/range`, { 
      params: { startTime, endTime } 
    }),
  getAverage: (resourceId, startTime, endTime) => 
    api.get(`/monitoring/resources/${resourceId}/average`, { 
      params: { startTime, endTime } 
    }),
};

export const scalingApi = {
  getByResource: (resourceId) => 
    api.get(`/scaling/resources/${resourceId}/rules`),
  create: (data) => api.post('/scaling/rules', data),
  disable: (id) => api.put(`/scaling/rules/${id}/disable`),
  evaluate: () => api.post('/scaling/evaluate'),
};

export const billingApi = {
  getBills: (customerId) => 
    api.get(`/billing/customers/${customerId}/bills`),
  getUsage: (customerId, startDate, endDate) => 
    api.get(`/billing/customers/${customerId}/usage`, { 
      params: { startDate, endDate } 
    }),
  getTotalCost: (customerId, startDate, endDate) => 
    api.get(`/billing/customers/${customerId}/total-cost`, { 
      params: { startDate, endDate } 
    }),
  generateDaily: () => api.post('/billing/generate-daily'),
  checkOverdue: () => api.post('/billing/check-overdue'),
};

export default api;
