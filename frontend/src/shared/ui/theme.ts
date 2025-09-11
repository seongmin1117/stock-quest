import { createTheme } from '@mui/material/styles';
import { koKR } from '@mui/material/locale';

// StockQuest Professional Trading Platform Theme
export const theme = createTheme(
  {
    palette: {
      mode: 'dark',
      primary: {
        main: '#2196F3',     // Professional Blue
        light: '#42A5F5',
        dark: '#1976D2',
        contrastText: '#ffffff',
      },
      secondary: {
        main: '#90CAF9',     // Light Blue accent
        light: '#BBDEFB',
        dark: '#64B5F6',
        contrastText: '#000000',
      },
      error: {
        main: '#F44336',     // Trading Red (bearish)
        light: '#EF5350',
        dark: '#D32F2F',
        contrastText: '#ffffff',
      },
      warning: {
        main: '#FF9800',     // Amber (neutral)
        light: '#FFB74D',
        dark: '#F57C00',
        contrastText: '#000000',
      },
      info: {
        main: '#2196F3',     // Info Blue
        light: '#42A5F5',
        dark: '#1976D2',
        contrastText: '#ffffff',
      },
      success: {
        main: '#4CAF50',     // Trading Green (bullish)
        light: '#66BB6A',
        dark: '#388E3C',
        contrastText: '#ffffff',
      },
      background: {
        default: '#0A0E18',  // Deep dark background
        paper: '#1A1F2E',   // Card/paper background
      },
      text: {
        primary: '#FFFFFF',     // Primary text
        secondary: '#B0BEC5',  // Secondary text
        disabled: '#78828A',    // Disabled text
      },
      divider: '#2A3441',       // Divider lines
      // Custom colors for trading
      common: {
        black: '#000000',
        white: '#FFFFFF',
      },
    },
    typography: {
      fontFamily: [
        '"Inter"',
        '"Roboto Mono"',    // Monospace for numbers
        '"SF Pro Display"',
        '-apple-system',
        'BlinkMacSystemFont',
        '"Segoe UI"',
        'Roboto',
        '"Helvetica Neue"',
        'Arial',
        'sans-serif',
      ].join(','),
      h1: {
        fontSize: '2.5rem',
        fontWeight: 700,
        lineHeight: 1.2,
        letterSpacing: '-0.01em',
        color: '#FFFFFF',
      },
      h2: {
        fontSize: '2rem',
        fontWeight: 600,
        lineHeight: 1.3,
        letterSpacing: '-0.01em',
        color: '#FFFFFF',
      },
      h3: {
        fontSize: '1.5rem',
        fontWeight: 600,
        lineHeight: 1.4,
        color: '#FFFFFF',
      },
      h4: {
        fontSize: '1.25rem',
        fontWeight: 600,
        lineHeight: 1.4,
        color: '#FFFFFF',
      },
      h5: {
        fontSize: '1.125rem',
        fontWeight: 600,
        lineHeight: 1.4,
        color: '#FFFFFF',
      },
      h6: {
        fontSize: '1rem',
        fontWeight: 600,
        lineHeight: 1.5,
        color: '#FFFFFF',
      },
      body1: {
        fontSize: '0.875rem',
        lineHeight: 1.5,
        color: '#B0BEC5',
      },
      body2: {
        fontSize: '0.75rem',
        lineHeight: 1.5,
        color: '#78828A',
      },
      // Custom typography for trading data
      caption: {
        fontSize: '0.75rem',
        lineHeight: 1.4,
        color: '#78828A',
        fontFamily: '"Roboto Mono", monospace',
      },
    },
    shape: {
      borderRadius: 8,  // More professional, less rounded
    },
    components: {
      // Professional Trading Platform Components
      MuiButton: {
        styleOverrides: {
          root: {
            textTransform: 'none',
            fontWeight: 500,
            borderRadius: 6,
            padding: '8px 16px',
            fontSize: '0.875rem',
            boxShadow: 'none',
            transition: 'all 0.2s ease-in-out',
            '&:hover': {
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)',
              transform: 'translateY(-1px)',
            },
          },
          contained: {
            backgroundColor: '#2196F3',
            color: '#FFFFFF',
            '&:hover': {
              backgroundColor: '#1976D2',
            },
            '&.MuiButton-containedSuccess': {
              backgroundColor: '#4CAF50',
              '&:hover': {
                backgroundColor: '#388E3C',
              },
            },
            '&.MuiButton-containedError': {
              backgroundColor: '#F44336',
              '&:hover': {
                backgroundColor: '#D32F2F',
              },
            },
          },
          outlined: {
            border: '1px solid #2A3441',
            color: '#B0BEC5',
            backgroundColor: 'transparent',
            '&:hover': {
              backgroundColor: 'rgba(33, 150, 243, 0.08)',
              border: '1px solid #2196F3',
            },
          },
          text: {
            color: '#B0BEC5',
            '&:hover': {
              backgroundColor: 'rgba(33, 150, 243, 0.08)',
            },
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            backgroundColor: '#1A1F2E',
            border: '1px solid #2A3441',
            borderRadius: 8,
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)',
            transition: 'all 0.2s ease-in-out',
            '&:hover': {
              border: '1px solid #2196F3',
              boxShadow: '0 4px 16px rgba(33, 150, 243, 0.1)',
            },
          },
        },
      },
      MuiTextField: {
        defaultProps: {
          variant: 'outlined',
          size: 'small',
        },
        styleOverrides: {
          root: {
            '& .MuiOutlinedInput-root': {
              backgroundColor: '#0A0E18',
              borderRadius: 6,
              fontSize: '0.875rem',
              transition: 'all 0.2s ease-in-out',
              '& fieldset': {
                border: '1px solid #2A3441',
              },
              '&:hover fieldset': {
                border: '1px solid #2196F3',
              },
              '&.Mui-focused fieldset': {
                border: '1px solid #2196F3',
                boxShadow: '0 0 0 2px rgba(33, 150, 243, 0.1)',
              },
              '& input': {
                color: '#FFFFFF',
                fontFamily: '"Roboto Mono", monospace', // For number inputs
              },
            },
            '& .MuiInputLabel-root': {
              color: '#78828A',
              fontSize: '0.875rem',
              '&.Mui-focused': {
                color: '#2196F3',
              },
            },
          },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundColor: '#1A1F2E',
            borderBottom: '1px solid #2A3441',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.12)',
          },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: {
            borderRadius: 4,
            fontWeight: 500,
            fontSize: '0.75rem',
            height: 24,
            backgroundColor: '#2A3441',
            color: '#B0BEC5',
            border: 'none',
            '&.MuiChip-colorSuccess': {
              backgroundColor: 'rgba(76, 175, 80, 0.15)',
              color: '#4CAF50',
            },
            '&.MuiChip-colorError': {
              backgroundColor: 'rgba(244, 67, 54, 0.15)',
              color: '#F44336',
            },
          },
        },
      },
      MuiToggleButton: {
        styleOverrides: {
          root: {
            borderRadius: 4,
            border: '1px solid #2A3441',
            backgroundColor: 'transparent',
            color: '#B0BEC5',
            textTransform: 'none',
            fontSize: '0.875rem',
            fontWeight: 500,
            transition: 'all 0.2s ease-in-out',
            '&:hover': {
              backgroundColor: 'rgba(33, 150, 243, 0.08)',
              border: '1px solid #2196F3',
            },
            '&.Mui-selected': {
              backgroundColor: '#2196F3',
              color: '#FFFFFF',
              border: '1px solid #2196F3',
              '&:hover': {
                backgroundColor: '#1976D2',
              },
            },
          },
        },
      },
      // Table components for trading data
      MuiTableContainer: {
        styleOverrides: {
          root: {
            backgroundColor: '#1A1F2E',
            border: '1px solid #2A3441',
            borderRadius: 8,
          },
        },
      },
      MuiTableHead: {
        styleOverrides: {
          root: {
            backgroundColor: '#0A0E18',
          },
        },
      },
      MuiTableCell: {
        styleOverrides: {
          root: {
            borderColor: '#2A3441',
            padding: '8px 16px',
            fontSize: '0.75rem',
            fontFamily: '"Roboto Mono", monospace',
          },
          head: {
            color: '#78828A',
            fontWeight: 600,
            textTransform: 'uppercase',
            fontSize: '0.625rem',
            letterSpacing: '0.05em',
            backgroundColor: '#0A0E18',
          },
          body: {
            color: '#B0BEC5',
          },
        },
      },
      MuiTableRow: {
        styleOverrides: {
          root: {
            '&:hover': {
              backgroundColor: 'rgba(33, 150, 243, 0.05)',
            },
          },
        },
      },
    },
  },
  koKR
);