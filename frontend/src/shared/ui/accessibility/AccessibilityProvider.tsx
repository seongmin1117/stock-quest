'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import {
  Box,
  Snackbar,
  Alert,
  Switch,
  FormControlLabel,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Divider,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Accessibility,
  VolumeUp,
  VolumeOff,
  Visibility,
  VisibilityOff,
  Speed,
  SlowMotionVideo,
} from '@mui/icons-material';

interface AccessibilitySettings {
  reducedMotion: boolean;
  highContrast: boolean;
  screenReader: boolean;
  largeText: boolean;
  audioFeedback: boolean;
  keyboardNavigation: boolean;
  focusIndicators: boolean;
  announcements: boolean;
}

interface AccessibilityContextType {
  settings: AccessibilitySettings;
  updateSetting: (key: keyof AccessibilitySettings, value: boolean) => void;
  announce: (message: string, priority?: 'polite' | 'assertive') => void;
  isAccessibilityMode: boolean;
  showAccessibilityPanel: boolean;
  setShowAccessibilityPanel: (show: boolean) => void;
}

const defaultSettings: AccessibilitySettings = {
  reducedMotion: false,
  highContrast: false,
  screenReader: false,
  largeText: false,
  audioFeedback: false,
  keyboardNavigation: true,
  focusIndicators: true,
  announcements: true,
};

const AccessibilityContext = createContext<AccessibilityContextType | null>(null);

export const useAccessibility = () => {
  const context = useContext(AccessibilityContext);
  if (!context) {
    throw new Error('useAccessibility must be used within AccessibilityProvider');
  }
  return context;
};

interface AccessibilityProviderProps {
  children: React.ReactNode;
}

/**
 * Accessibility provider with WCAG 2.1 AA compliance features
 * WCAG 2.1 AA 준수 기능을 갖춘 접근성 프로바이더
 */
export const AccessibilityProvider: React.FC<AccessibilityProviderProps> = ({
  children
}) => {
  const theme = useTheme();
  const [settings, setSettings] = useState<AccessibilitySettings>(defaultSettings);
  const [showPanel, setShowPanel] = useState(false);
  const [announcements, setAnnouncements] = useState<Array<{
    id: string;
    message: string;
    priority: 'polite' | 'assertive';
    timestamp: number;
  }>>([]);

  // Detect user preferences from system
  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    const contrastQuery = window.matchMedia('(prefers-contrast: high)');

    setSettings(prev => ({
      ...prev,
      reducedMotion: mediaQuery.matches,
      highContrast: contrastQuery.matches,
    }));

    const handleMotionChange = (e: MediaQueryListEvent) => {
      setSettings(prev => ({ ...prev, reducedMotion: e.matches }));
    };

    const handleContrastChange = (e: MediaQueryListEvent) => {
      setSettings(prev => ({ ...prev, highContrast: e.matches }));
    };

    mediaQuery.addEventListener('change', handleMotionChange);
    contrastQuery.addEventListener('change', handleContrastChange);

    return () => {
      mediaQuery.removeEventListener('change', handleMotionChange);
      contrastQuery.removeEventListener('change', handleContrastChange);
    };
  }, []);

  // Apply accessibility styles
  useEffect(() => {
    const root = document.documentElement;

    // High contrast mode
    if (settings.highContrast) {
      root.style.setProperty('--accessibility-contrast', '1.5');
      root.style.filter = 'contrast(1.5)';
    } else {
      root.style.setProperty('--accessibility-contrast', '1');
      root.style.filter = 'none';
    }

    // Large text mode
    if (settings.largeText) {
      root.style.fontSize = '120%';
    } else {
      root.style.fontSize = '100%';
    }

    // Reduced motion
    if (settings.reducedMotion) {
      root.style.setProperty('--accessibility-motion', 'none');
    } else {
      root.style.setProperty('--accessibility-motion', 'auto');
    }

    // Focus indicators
    if (settings.focusIndicators) {
      root.style.setProperty('--accessibility-focus-width', '3px');
      root.style.setProperty('--accessibility-focus-style', 'solid');
    } else {
      root.style.setProperty('--accessibility-focus-width', '1px');
      root.style.setProperty('--accessibility-focus-style', 'dotted');
    }
  }, [settings]);

  // Keyboard navigation
  useEffect(() => {
    if (!settings.keyboardNavigation) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      // Global keyboard shortcuts
      switch (e.key) {
        case 'F1':
          if (e.altKey) {
            e.preventDefault();
            setShowPanel(true);
            announce('접근성 설정 패널이 열렸습니다', 'assertive');
          }
          break;
        case 'Escape':
          if (showPanel) {
            setShowPanel(false);
            announce('접근성 설정 패널이 닫혔습니다', 'polite');
          }
          break;
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [settings.keyboardNavigation, showPanel]);

  const updateSetting = useCallback((key: keyof AccessibilitySettings, value: boolean) => {
    setSettings(prev => {
      const newSettings = { ...prev, [key]: value };

      // Announce setting changes
      if (newSettings.announcements) {
        const settingNames: Record<keyof AccessibilitySettings, string> = {
          reducedMotion: '동작 줄이기',
          highContrast: '고대비 모드',
          screenReader: '스크린 리더 지원',
          largeText: '큰 텍스트',
          audioFeedback: '오디오 피드백',
          keyboardNavigation: '키보드 탐색',
          focusIndicators: '포커스 표시',
          announcements: '음성 안내',
        };

        announce(
          `${settingNames[key]}가 ${value ? '활성화' : '비활성화'}되었습니다`,
          'polite'
        );
      }

      return newSettings;
    });
  }, []);

  const announce = useCallback((
    message: string,
    priority: 'polite' | 'assertive' = 'polite'
  ) => {
    if (!settings.announcements) return;

    const announcement = {
      id: Date.now().toString(),
      message,
      priority,
      timestamp: Date.now(),
    };

    setAnnouncements(prev => [...prev, announcement]);

    // Auto-remove after delay
    setTimeout(() => {
      setAnnouncements(prev => prev.filter(a => a.id !== announcement.id));
    }, 5000);

    // Audio feedback if enabled
    if (settings.audioFeedback && 'speechSynthesis' in window) {
      const utterance = new SpeechSynthesisUtterance(message);
      utterance.lang = 'ko-KR';
      utterance.rate = 0.9;
      utterance.pitch = 1;
      speechSynthesis.speak(utterance);
    }
  }, [settings.announcements, settings.audioFeedback]);

  const isAccessibilityMode = Object.values(settings).some(Boolean);

  const contextValue: AccessibilityContextType = {
    settings,
    updateSetting,
    announce,
    isAccessibilityMode,
    showAccessibilityPanel: showPanel,
    setShowAccessibilityPanel: setShowPanel,
  };

  return (
    <AccessibilityContext.Provider value={contextValue}>
      {/* Apply global accessibility styles */}
      <Box
        sx={{
          '--accessibility-focus-color': theme.palette.primary.main,
          '*:focus': {
            outline: settings.focusIndicators
              ? `var(--accessibility-focus-width, 2px) var(--accessibility-focus-style, solid) var(--accessibility-focus-color)`
              : 'none',
            outlineOffset: '2px',
          },
          '*, *::before, *::after': {
            animationDuration: settings.reducedMotion ? '0.01ms !important' : undefined,
            animationIterationCount: settings.reducedMotion ? '1 !important' : undefined,
            transitionDuration: settings.reducedMotion ? '0.01ms !important' : undefined,
          },
        }}
      >
        {children}
      </Box>

      {/* Live regions for announcements */}
      <Box
        component="div"
        role="log"
        aria-live="polite"
        aria-label="일반 알림"
        sx={{
          position: 'absolute',
          left: '-10000px',
          width: '1px',
          height: '1px',
          overflow: 'hidden',
        }}
      >
        {announcements
          .filter(a => a.priority === 'polite')
          .map(a => (
            <div key={a.id}>{a.message}</div>
          ))}
      </Box>

      <Box
        component="div"
        role="alert"
        aria-live="assertive"
        aria-label="중요 알림"
        sx={{
          position: 'absolute',
          left: '-10000px',
          width: '1px',
          height: '1px',
          overflow: 'hidden',
        }}
      >
        {announcements
          .filter(a => a.priority === 'assertive')
          .map(a => (
            <div key={a.id}>{a.message}</div>
          ))}
      </Box>

      {/* Accessibility Panel */}
      <AccessibilityPanel
        open={showPanel}
        onClose={() => setShowPanel(false)}
        settings={settings}
        onUpdateSetting={updateSetting}
      />

      {/* Status indicator */}
      {isAccessibilityMode && (
        <Box
          sx={{
            position: 'fixed',
            top: 16,
            right: 16,
            zIndex: 1400,
            backgroundColor: alpha(theme.palette.info.main, 0.9),
            color: theme.palette.info.contrastText,
            borderRadius: 1,
            px: 1,
            py: 0.5,
            fontSize: '0.75rem',
            display: 'flex',
            alignItems: 'center',
            gap: 0.5,
          }}
          role="status"
          aria-label="접근성 모드 활성"
        >
          <Accessibility fontSize="small" />
          접근성 모드
        </Box>
      )}
    </AccessibilityContext.Provider>
  );
};

interface AccessibilityPanelProps {
  open: boolean;
  onClose: () => void;
  settings: AccessibilitySettings;
  onUpdateSetting: (key: keyof AccessibilitySettings, value: boolean) => void;
}

const AccessibilityPanel: React.FC<AccessibilityPanelProps> = ({
  open,
  onClose,
  settings,
  onUpdateSetting,
}) => {
  const theme = useTheme();

  const settingGroups = [
    {
      title: '시각적 설정',
      items: [
        {
          key: 'highContrast' as const,
          label: '고대비 모드',
          description: '텍스트와 배경 간의 대비를 높입니다',
          icon: <Visibility />,
        },
        {
          key: 'largeText' as const,
          label: '큰 텍스트',
          description: '텍스트 크기를 20% 늘립니다',
          icon: <Typography fontSize="large">A</Typography>,
        },
        {
          key: 'focusIndicators' as const,
          label: '포커스 표시 강화',
          description: '키보드 포커스를 더 명확하게 표시합니다',
          icon: <Box sx={{ border: '2px solid', borderRadius: 1, p: 0.5 }}>□</Box>,
        },
      ],
    },
    {
      title: '동작 및 애니메이션',
      items: [
        {
          key: 'reducedMotion' as const,
          label: '동작 줄이기',
          description: '애니메이션과 전환 효과를 최소화합니다',
          icon: <SlowMotionVideo />,
        },
      ],
    },
    {
      title: '오디오 및 피드백',
      items: [
        {
          key: 'audioFeedback' as const,
          label: '오디오 피드백',
          description: '중요한 알림을 음성으로 안내합니다',
          icon: <VolumeUp />,
        },
        {
          key: 'announcements' as const,
          label: '음성 안내',
          description: '스크린 리더를 위한 실시간 안내를 제공합니다',
          icon: <VolumeUp />,
        },
      ],
    },
    {
      title: '탐색 및 상호작용',
      items: [
        {
          key: 'keyboardNavigation' as const,
          label: '키보드 탐색',
          description: '키보드만으로 모든 기능을 사용할 수 있습니다',
          icon: <Speed />,
        },
        {
          key: 'screenReader' as const,
          label: '스크린 리더 지원',
          description: '스크린 리더 사용자를 위한 추가 정보를 제공합니다',
          icon: <Accessibility />,
        },
      ],
    },
  ];

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      aria-labelledby="accessibility-dialog-title"
      PaperProps={{
        sx: {
          maxHeight: '80vh',
        },
      }}
    >
      <DialogTitle id="accessibility-dialog-title">
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Accessibility />
          접근성 설정
        </Box>
      </DialogTitle>

      <DialogContent>
        <Typography variant="body2" color="textSecondary" sx={{ mb: 3 }}>
          이 설정들은 Stock Quest를 더 접근하기 쉽게 만들어줍니다.
          <br />
          단축키: Alt + F1 (설정 열기), Escape (설정 닫기)
        </Typography>

        {settingGroups.map((group, groupIndex) => (
          <Box key={group.title} sx={{ mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              {group.title}
            </Typography>

            {group.items.map((item) => (
              <Box
                key={item.key}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 2,
                  p: 2,
                  borderRadius: 2,
                  backgroundColor: alpha(theme.palette.action.hover, 0.5),
                  mb: 1,
                }}
              >
                <Box sx={{ color: 'primary.main', minWidth: 40 }}>
                  {item.icon}
                </Box>
                <Box sx={{ flex: 1 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    {item.label}
                  </Typography>
                  <Typography variant="caption" color="textSecondary">
                    {item.description}
                  </Typography>
                </Box>
                <FormControlLabel
                  control={
                    <Switch
                      checked={settings[item.key]}
                      onChange={(e) => onUpdateSetting(item.key, e.target.checked)}
                      color="primary"
                    />
                  }
                  label=""
                  sx={{ m: 0 }}
                />
              </Box>
            ))}

            {groupIndex < settingGroups.length - 1 && (
              <Divider sx={{ mt: 2 }} />
            )}
          </Box>
        ))}

        <Box
          sx={{
            mt: 3,
            p: 2,
            backgroundColor: alpha(theme.palette.info.main, 0.1),
            borderRadius: 2,
            border: `1px solid ${alpha(theme.palette.info.main, 0.3)}`,
          }}
        >
          <Typography variant="subtitle2" color="info.main" gutterBottom>
            💡 접근성 팁
          </Typography>
          <Typography variant="body2" color="textSecondary">
            • Tab 키로 요소 간 이동<br />
            • Enter 또는 Space로 버튼 활성화<br />
            • 화살표 키로 탭과 메뉴 탐색<br />
            • Escape로 대화상자 닫기
          </Typography>
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} autoFocus>
          확인
        </Button>
      </DialogActions>
    </Dialog>
  );
};