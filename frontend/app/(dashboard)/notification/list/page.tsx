'use client';

import { useState, useEffect } from 'react';
import api from '@/lib/api/client';

interface Notification {
  id: number;
  notificationType: string;
  title: string;
  content: string;
  receiverId: number;
  receiverName: string;
  sendStatus: number;
  sendTime: string;
  priority: number;
  isRead: boolean;
  readTime: string;
  bizType: string;
  bizId: number;
  createdAt: string;
}

export default function NotificationListPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'unread'>('all');

  useEffect(() => {
    fetchData();
  }, [filter]);

  const fetchData = async () => {
    try {
      const userId = localStorage.getItem('userId') || '1';
      const [listRes, countRes] = await Promise.all([
        api.get(`/notification/v1/notifications/${filter === 'unread' ? 'unread' : 'all'}?receiverId=${userId}`),
        api.get(`/notification/v1/notifications/unread/count?receiverId=${userId}`)
      ]);
      setNotifications(listRes.data || []);
      setUnreadCount(countRes.data || 0);
    } catch (error) {
      console.error('获取通知失败', error);
    } finally {
      setLoading(false);
    }
  };

  const markAsRead = async (id: number) => {
    try {
      await api.put(`/notification/v1/notifications/${id}/read`);
      fetchData();
    } catch (error) {
      console.error('标记已读失败', error);
    }
  };

  const markAllAsRead = async () => {
    try {
      const userId = localStorage.getItem('userId') || '1';
      await api.put(`/notification/v1/notifications/all/read?receiverId=${userId}`);
      fetchData();
    } catch (error) {
      console.error('全部标记已读失败', error);
    }
  };

  const getTypeBadge = (type: string) => {
    switch (type) {
      case 'system':
        return <span className="px-2 py-0.5 text-xs rounded-full bg-blue-100 text-blue-800">系统</span>;
      case 'email':
        return <span className="px-2 py-0.5 text-xs rounded-full bg-green-100 text-green-800">邮件</span>;
      case 'sms':
        return <span className="px-2 py-0.5 text-xs rounded-full bg-yellow-100 text-yellow-800">短信</span>;
      case 'push':
        return <span className="px-2 py-0.5 text-xs rounded-full bg-purple-100 text-purple-800">推送</span>;
      default:
        return <span className="px-2 py-0.5 text-xs rounded-full bg-gray-100 text-gray-800">{type}</span>;
    }
  };

  const getPriorityDot = (priority: number) => {
    switch (priority) {
      case 2:
        return <span className="w-2 h-2 rounded-full bg-red-500 inline-block" title="紧急" />;
      case 1:
        return <span className="w-2 h-2 rounded-full bg-yellow-500 inline-block" title="重要" />;
      default:
        return <span className="w-2 h-2 rounded-full bg-gray-300 inline-block" title="普通" />;
    }
  };

  if (loading) {
    return <div className="p-6">加载中...</div>;
  }

  return (
    <div className="p-6">
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">消息中心</h1>
          <p className="text-gray-500">{unreadCount > 0 ? `${unreadCount} 条未读消息` : '没有未读消息'}</p>
        </div>
        <div className="flex gap-2">
          {unreadCount > 0 && (
            <button
              onClick={markAllAsRead}
              className="px-4 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50"
            >
              全部标记已读
            </button>
          )}
        </div>
      </div>

      {/* 筛选标签 */}
      <div className="mb-4 flex gap-2">
        <button
          onClick={() => setFilter('all')}
          className={`px-4 py-2 rounded-lg text-sm ${filter === 'all' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700'}`}
        >
          全部消息
        </button>
        <button
          onClick={() => setFilter('unread')}
          className={`px-4 py-2 rounded-lg text-sm ${filter === 'unread' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700'}`}
        >
          未读消息 {unreadCount > 0 && `(${unreadCount})`}
        </button>
      </div>

      {/* 通知列表 */}
      <div className="bg-white rounded-lg shadow divide-y">
        {notifications.map((notification) => (
          <div
            key={notification.id}
            className={`p-4 hover:bg-gray-50 cursor-pointer ${!notification.isRead ? 'bg-blue-50' : ''}`}
            onClick={() => !notification.isRead && markAsRead(notification.id)}
          >
            <div className="flex items-start gap-3">
              <div className="mt-1.5">{getPriorityDot(notification.priority)}</div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  {getTypeBadge(notification.notificationType)}
                  <span className="font-medium text-gray-900 truncate">
                    {notification.title}
                  </span>
                  {!notification.isRead && (
                    <span className="w-2 h-2 rounded-full bg-blue-500 inline-block flex-shrink-0" />
                  )}
                </div>
                <p className="text-sm text-gray-600 line-clamp-2">
                  {notification.content}
                </p>
                <div className="flex items-center gap-4 mt-2 text-xs text-gray-400">
                  <span>{new Date(notification.createdAt).toLocaleString('zh-CN')}</span>
                  {notification.bizType && (
                    <span className="text-gray-500">关联: {notification.bizType}</span>
                  )}
                </div>
              </div>
            </div>
          </div>
        ))}
        {notifications.length === 0 && (
          <div className="py-12 text-center text-gray-500">
            {filter === 'unread' ? '没有未读消息' : '暂无消息'}
          </div>
        )}
      </div>
    </div>
  );
}
