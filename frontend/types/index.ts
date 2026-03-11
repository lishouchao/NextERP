// 用户相关类型
export interface User {
  id: number;
  username: string;
  realName: string | null;
  email: string | null;
  phone: string | null;
  avatar: string | null;
  deptId: number | null;
  deptName: string | null;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userInfo: User;
  permissions: string[];
  roles: string[];
  menus: Menu[];
}

export interface Menu {
  id: number;
  name: string;
  path: string;
  icon: string | null;
  parentId: number | null;
  sort: number;
  children?: Menu[];
}

// 通用响应类型
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
  error: boolean;
  success: boolean;
}

// 分页类型
export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export interface PageRequest {
  current?: number;
  size?: number;
}
