'use client';

import React, { useState, useEffect, useRef } from 'react';
import {
  TextField,
  Autocomplete,
  Box,
  Typography,
  Avatar,
  Chip,
  CircularProgress,
  Paper,
  Grid,
  Divider,
  IconButton,
  Tooltip,
} from '@mui/material';
import { Business, TrendingUp, Search } from '@mui/icons-material';
import { companyClient, Company, PopularCompany, CompanyCategory } from '@/shared/api/company-client';

interface CompanyAutocompleteProps {
  value?: string;
  onChange: (symbol: string, company?: Company) => void;
  label?: string;
  placeholder?: string;
  error?: boolean;
  helperText?: string;
  fullWidth?: boolean;
  size?: 'small' | 'medium';
}

/**
 * 한국 회사 자동완성 컴포넌트
 */
export default function CompanyAutocomplete({
  value,
  onChange,
  label = '회사 검색',
  placeholder = '삼성전자, Samsung, 005930',
  error = false,
  helperText,
  fullWidth = true,
  size = 'small'
}: CompanyAutocompleteProps) {
  const [open, setOpen] = useState(false);
  const [options, setOptions] = useState<Company[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [popularCompanies, setPopularCompanies] = useState<PopularCompany[]>([]);
  const [categories, setCategories] = useState<CompanyCategory[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [selectedCompany, setSelectedCompany] = useState<Company | null>(null);

  const searchTimeoutRef = useRef<NodeJS.Timeout>();

  // 디버깅용 로깅
  console.log('🔍 [CompanyAutocomplete] Component render state:', {
    categoriesCount: categories?.length || 0,
    popularCompaniesCount: popularCompanies?.length || 0,
    optionsCount: options?.length || 0,
    loading,
    searchQuery,
    selectedCategory,
    open
  });

  // 인기 회사와 카테고리 로드
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        console.log('🔍 [CompanyAutocomplete] Loading initial data...');
        const [popularData, categoriesData] = await Promise.all([
          companyClient.getPopular(8),
          companyClient.getCategories()
        ]);
        console.log('✅ [CompanyAutocomplete] Popular companies loaded:', popularData?.length || 0);
        console.log('✅ [CompanyAutocomplete] Categories loaded:', categoriesData?.length || 0);
        setPopularCompanies(Array.isArray(popularData) ? popularData : []);
        setCategories(Array.isArray(categoriesData) ? categoriesData : []);
      } catch (error: any) {
        console.error('❌ [CompanyAutocomplete] Failed to load initial data:', error);
        if (error.response) {
          console.error('Response status:', error.response.status);
          console.error('Response data:', error.response.data);
        }
      }
    };

    loadInitialData();
  }, []);

  // 검색어가 변경될 때 API 호출
  useEffect(() => {
    if (!open) return;

    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    searchTimeoutRef.current = setTimeout(async () => {
      if (searchQuery.trim() === '' && selectedCategory === '') {
        // 검색어가 없고 카테고리도 선택 안됨 -> 인기 회사 표시
        const popularAsCompanies: Company[] = (popularCompanies || []).map(p => ({
          id: p.id,
          symbol: p.symbol,
          nameKr: p.nameKr,
          nameEn: p.nameEn,
          sector: p.sector,
          marketCap: p.marketCap,
          marketCapDisplay: p.marketCapDisplay,
          logoPath: p.logoPath,
          descriptionKr: '',
          descriptionEn: '',
          exchange: 'KRX',
          currency: 'KRW',
          isActive: true,
          popularityScore: p.popularityScore,
          categories: p.categories
        }));
        setOptions(popularAsCompanies);
        return;
      }

      setLoading(true);
      try {
        const searchParams = {
          q: searchQuery.trim() || undefined,
          categories: selectedCategory ? [selectedCategory] : undefined,
          limit: 10
        };
        console.log('🔍 [CompanyAutocomplete] Searching with params:', searchParams);
        const response = await companyClient.search(searchParams);
        console.log('✅ [CompanyAutocomplete] Search results:', response.companies.length, 'companies');
        setOptions(response.companies);
      } catch (error: any) {
        console.error('❌ [CompanyAutocomplete] Search failed:', error);
        if (error.response) {
          console.error('Response status:', error.response.status);
          console.error('Response data:', error.response.data);
        }
        setOptions([]);
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, [searchQuery, selectedCategory, open, popularCompanies]);

  // value prop이 변경될 때만 선택된 회사 찾기
  useEffect(() => {
    if (!value) {
      if (selectedCompany) {
        setSelectedCompany(null);
      }
      return;
    }

    // 이미 올바른 회사가 선택되어 있으면 API 호출 안함
    if (selectedCompany?.symbol === value) {
      return;
    }

    const findCompany = async () => {
      try {
        console.log('🔍 [CompanyAutocomplete] Loading company details for:', value);
        const company = await companyClient.getBySymbol(value);
        const companyData: Company = {
          id: company.id,
          symbol: company.symbol,
          nameKr: company.nameKr,
          nameEn: company.nameEn,
          sector: company.sector,
          marketCap: company.marketCap,
          marketCapDisplay: company.marketCapDisplay,
          logoPath: company.logoPath,
          descriptionKr: company.descriptionKr,
          descriptionEn: company.descriptionEn,
          exchange: company.exchange,
          currency: company.currency,
          isActive: company.isActive,
          popularityScore: company.popularityScore,
          categories: company.categories
        };
        console.log('✅ [CompanyAutocomplete] Company details loaded:', companyData.nameKr);
        setSelectedCompany(companyData);
      } catch (error) {
        console.error('❌ [CompanyAutocomplete] Failed to find company:', error);
        setSelectedCompany(null);
      }
    };

    findCompany();
  }, [value]); // value가 변경될 때만 실행

  const handleCompanySelect = (company: Company | null) => {
    setSelectedCompany(company);
    onChange(company?.symbol || '', company || undefined);
    setOpen(false);
  };

  const handlePopularCompanyClick = (company: PopularCompany) => {
    const companyData: Company = {
      id: company.id,
      symbol: company.symbol,
      nameKr: company.nameKr,
      nameEn: company.nameEn,
      sector: company.sector,
      marketCap: company.marketCap,
      marketCapDisplay: company.marketCapDisplay,
      logoPath: company.logoPath,
      descriptionKr: '',
      descriptionEn: '',
      exchange: 'KRX',
      currency: 'KRW',
      isActive: true,
      popularityScore: company.popularityScore,
      categories: company.categories
    };
    handleCompanySelect(companyData);
  };

  const handleCategoryClick = (categoryId: string) => {
    setSelectedCategory(selectedCategory === categoryId ? '' : categoryId);
  };

  // 명시적 검색 실행 함수 (사용자 경험 개선)
  const handleExplicitSearch = () => {
    if (!open) {
      setOpen(true);
    }

    // 이미 검색어가 있다면 즉시 검색
    if (searchQuery.trim()) {
      // 디바운스 타이머 취소하고 즉시 검색 실행
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }

      setLoading(true);
      const performSearch = async () => {
        try {
          const searchParams = {
            q: searchQuery.trim(),
            categories: selectedCategory ? [selectedCategory] : undefined,
            limit: 10
          };
          console.log('🔍 [CompanyAutocomplete] 명시적 검색 실행:', searchParams);
          const response = await companyClient.search(searchParams);
          console.log('✅ [CompanyAutocomplete] 명시적 검색 결과:', response.companies.length, 'companies');
          setOptions(response.companies);
        } catch (error: any) {
          console.error('❌ [CompanyAutocomplete] 명시적 검색 실패:', error);
          setOptions([]);
        } finally {
          setLoading(false);
        }
      };

      performSearch();
    }
  };

  const renderOption = (props: any, option: Company) => (
    <Box component="li" {...props} data-testid={`company-option-${option.symbol}`}>
      <Box display="flex" alignItems="center" gap={2} width="100%">
        <Avatar
          src={option.logoPath}
          sx={{ width: 32, height: 32 }}
        >
          <Business />
        </Avatar>
        <Box flex={1}>
          <Typography variant="body2" fontWeight="medium">
            {option.nameKr} ({option.nameEn})
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {option.symbol} • {option.sector}
            {option.marketCapDisplay && ` • ${option.marketCapDisplay}`}
          </Typography>
        </Box>
      </Box>
    </Box>
  );


  return (
    <Box>
      {/* 카테고리 필터 */}
      <Box mb={2}>
        <Typography variant="caption" color="text.secondary" gutterBottom>
          카테고리 선택
        </Typography>
        <Box display="flex" flexWrap="wrap" gap={1} mt={1}>
          <Chip
            label="전체"
            size="small"
            variant={selectedCategory === '' ? "filled" : "outlined"}
            onClick={() => handleCategoryClick('')}
            sx={{
              borderColor: selectedCategory === '' ? 'primary.main' : 'divider'
            }}
          />
          {(categories || []).map((category) => (
            <Chip
              key={category.categoryId}
              label={category.nameKr}
              size="small"
              variant={selectedCategory === category.categoryId ? "filled" : "outlined"}
              onClick={() => handleCategoryClick(category.categoryId)}
              data-testid={`category-filter-${category.categoryId}`}
              sx={{
                borderColor: selectedCategory === category.categoryId ? 'primary.main' : 'divider'
              }}
            />
          ))}
        </Box>
      </Box>

      {/* 인기 종목 (검색어가 없을 때) */}
      {searchQuery.trim() === '' && selectedCategory === '' && (
        <Box mb={2}>
          <Typography variant="caption" color="text.secondary" gutterBottom>
            인기 종목
          </Typography>
          <Grid container spacing={1} mt={1}>
            {(popularCompanies || []).map((company) => (
              <Grid item xs={6} sm={4} md={3} key={company.symbol}>
                <Box
                  data-testid={`popular-company-${company.symbol}`}
                  onClick={() => handlePopularCompanyClick(company)}
                  sx={{
                    p: 1.5,
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 1,
                    cursor: 'pointer',
                    '&:hover': {
                      borderColor: 'primary.main',
                      bgcolor: 'action.hover'
                    }
                  }}
                >
                  <Box display="flex" alignItems="center" gap={1}>
                    <Avatar src={company.logoPath} sx={{ width: 24, height: 24 }}>
                      <Business />
                    </Avatar>
                    <Box flex={1} minWidth={0}>
                      <Typography variant="body2" fontWeight="medium" noWrap>
                        {company.nameKr}
                      </Typography>
                      <Typography variant="caption" color="text.secondary" noWrap>
                        {company.marketCapDisplay}
                      </Typography>
                    </Box>
                  </Box>
                </Box>
              </Grid>
            ))}
          </Grid>
        </Box>
      )}

      <Autocomplete
        open={open}
        onOpen={() => setOpen(true)}
        onClose={() => setOpen(false)}
        value={selectedCompany}
        onChange={(event, newValue) => handleCompanySelect(newValue)}
        inputValue={searchQuery}
        onInputChange={(event, newInputValue) => setSearchQuery(newInputValue)}
        options={options}
        getOptionLabel={(option) => `${option.nameKr} (${option.symbol})`}
        renderOption={renderOption}
        filterOptions={(x) => x} // 필터링 비활성화 (서버에서 처리)
        fullWidth={fullWidth}
        size={size}
        loading={loading}
        renderInput={(params) => (
          <TextField
            {...params}
            label={label}
            placeholder={placeholder}
            error={error}
            helperText={helperText}
            onKeyDown={(e) => {
              // 엔터 키로 검색 실행
              if (e.key === 'Enter' && !e.defaultPrevented) {
                e.preventDefault();
                handleExplicitSearch();
              }
            }}
            InputProps={{
              ...params.InputProps,
              endAdornment: (
                <>
                  {loading && <CircularProgress color="inherit" size={20} />}
                  {!loading && (
                    <Tooltip title="검색 실행 (Enter 또는 클릭)">
                      <IconButton
                        size="small"
                        onClick={handleExplicitSearch}
                        edge="end"
                        sx={{ mr: 1 }}
                        data-testid="company-search-button"
                      >
                        <Search fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  )}
                  {params.InputProps.endAdornment}
                </>
              ),
            }}
          />
        )}
        sx={{ mb: 2 }}
      />
    </Box>
  );
}