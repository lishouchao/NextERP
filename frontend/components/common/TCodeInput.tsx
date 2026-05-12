'use client';

import { useState, useRef, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Input, AutoComplete, Tag, Typography, message } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { findTCode, TCODES, type TCode } from '@/config/tcodes';

const { Text } = Typography;

// 历史记录存储键
const HISTORY_KEY = 'tcode_history';
const MAX_HISTORY = 5;

function getHistory(): string[] {
  if (typeof window === 'undefined') return [];
  try {
    const data = localStorage.getItem(HISTORY_KEY);
    return data ? JSON.parse(data) : [];
  } catch {
    return [];
  }
}

function saveHistory(code: string) {
  const history = getHistory().filter(c => c !== code);
  history.unshift(code);
  localStorage.setItem(HISTORY_KEY, JSON.stringify(history.slice(0, MAX_HISTORY)));
}

function getModuleColor(module: string): string {
  const colors: Record<string, string> = {
    finance: 'blue',
    hrm: 'green',
    supply: 'orange',
    sales: 'purple',
    production: 'cyan',
    settings: 'red',
    common: 'default',
  };
  return colors[module] || 'default';
}

interface TCodeSearchProps {
  style?: React.CSSProperties;
  className?: string;
}

export default function TCodeSearch({ style, className }: TCodeSearchProps) {
  const router = useRouter();
  const [value, setValue] = useState('');
  const [options, setOptions] = useState<{ value: string; label: React.ReactNode }[]>([]);
  const [focused, setFocused] = useState(false);
  const inputRef = useRef<any>(null);

  // 快捷键 Ctrl+T 聚焦
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 't') {
        e.preventDefault();
        inputRef.current?.focus();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  // 搜索
  const handleSearch = (searchValue: string) => {
    setValue(searchValue);

    if (!searchValue.trim()) {
      // 空值显示历史
      const history = getHistory();
      const historyOptions = history
        .map(code => findTCode(code))
        .filter(Boolean)
        .map((tcode: any) => ({
          value: tcode.code,
          label: (
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '4px 0' }}>
              <Text keyboard style={{ minWidth: 70 }}>{tcode.code}</Text>
              <Text type="secondary" style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {tcode.name}
              </Text>
              <Tag style={{ fontSize: 10, margin: 0 }} color="default">最近</Tag>
            </div>
          ),
        }));
      setOptions(historyOptions);
      return;
    }

    // 模糊搜索 - 优先匹配事务码开头
    const searchUpper = searchValue.toUpperCase();
    const matches = TCODES.filter(t => {
      const codeUpper = t.code.toUpperCase();
      const nameLower = t.name.toLowerCase();
      const searchLower = searchValue.toLowerCase();
      return codeUpper.startsWith(searchUpper) ||
             codeUpper.includes(searchUpper) ||
             nameLower.includes(searchLower);
    });

    // 按匹配度排序
    const sorted = matches.sort((a, b) => {
      const aCode = a.code.toUpperCase();
      const bCode = b.code.toUpperCase();
      const aStartsWith = aCode.startsWith(searchUpper);
      const bStartsWith = bCode.startsWith(searchUpper);
      if (aStartsWith && !bStartsWith) return -1;
      if (!aStartsWith && bStartsWith) return 1;
      return a.code.localeCompare(b.code);
    }).slice(0, 8);

    const searchOptions = sorted.map((tcode) => {
      const isCodeStartsWith = tcode.code.toUpperCase().startsWith(searchUpper);
      return {
        value: tcode.code,
        label: (
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '4px 0' }}>
            <Text keyboard style={{ minWidth: 70, fontWeight: isCodeStartsWith ? 600 : 400 }}>
              {tcode.code}
            </Text>
            <Text style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {tcode.name}
            </Text>
            <Tag color={getModuleColor(tcode.module)} style={{ fontSize: 10, margin: 0 }}>
              {tcode.module.toUpperCase()}
            </Tag>
          </div>
        ),
      };
    });

    setOptions(searchOptions);
  };

  // 选择
  const handleSelect = (selectedValue: string) => {
    const tcode = findTCode(selectedValue);
    if (tcode) {
      saveHistory(tcode.code);
      setValue('');
      setOptions([]);
      router.push(tcode.path);
    } else {
      message.error(`事务码 "${selectedValue}" 不存在`);
    }
  };

  // 回车执行
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && value.trim()) {
      e.preventDefault();
      const tcode = findTCode(value.trim());
      if (tcode) {
        handleSelect(tcode.code);
      } else if (options.length > 0) {
        handleSelect(options[0].value);
      } else {
        message.error(`事务码 "${value}" 不存在`);
      }
    }
  };

  return (
    <AutoComplete
      ref={inputRef}
      value={value}
      options={options}
      onSearch={handleSearch}
      onSelect={handleSelect}
      style={{ width: 280, ...style }}
      className={className}
    >
      <Input
        prefix={<SearchOutlined style={{ color: focused ? '#1890ff' : '#bfbfbf' }} />}
        placeholder="事务码 (Ctrl+T)"
        size="middle"
        onKeyDown={handleKeyDown}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        allowClear
        style={{
          borderRadius: 6,
          backgroundColor: focused ? '#fff' : '#fafafa',
          transition: 'all 0.2s',
        }}
      />
    </AutoComplete>
  );
}
