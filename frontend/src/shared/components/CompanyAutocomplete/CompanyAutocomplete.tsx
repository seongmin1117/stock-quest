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
} from '@mui/material';
import { Business, TrendingUp } from '@mui/icons-material';
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

  // 인기 회사와 카테고리 로드
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        const [popularData, categoriesData] = await Promise.all([
          companyClient.getPopular(8),
          companyClient.getCategories()
        ]);
        setPopularCompanies(popularData);
        setCategories(categoriesData);
      } catch (error) {
        console.error('Failed to load initial data:', error);
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
        const popularAsCompanies: Company[] = popularCompanies.map(p => ({
          symbol: p.symbol,
          nameKr: p.nameKr,
          nameEn: p.nameEn,
          sector: p.sector,
          logoPath: p.logoPath,
          marketCapDisplay: p.marketCap
        }));
        setOptions(popularAsCompanies);
        return;
      }

      setLoading(true);
      try {
        const response = await companyClient.search({
          q: searchQuery.trim() || undefined,
          category: selectedCategory || undefined,
          limit: 10
        });
        setOptions(response.companies);
      } catch (error) {
        console.error('Search failed:', error);
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

  // value prop이 변경될 때 선택된 회사 찾기
  useEffect(() => {
    if (value && value !== selectedCompany?.symbol) {
      const findCompany = async () => {
        try {
          const company = await companyClient.getBySymbol(value);
          const companyData: Company = {
            symbol: company.symbol,
            nameKr: company.nameKr,
            nameEn: company.nameEn,
            sector: company.sector,
            logoPath: company.logoPath,
            marketCapDisplay: company.marketCapDisplay
          };
          setSelectedCompany(companyData);
        } catch (error) {
          console.error('Failed to find company:', error);
          setSelectedCompany(null);
        }
      };
      findCompany();
    }
  }, [value, selectedCompany]);

  const handleCompanySelect = (company: Company | null) => {
    setSelectedCompany(company);
    onChange(company?.symbol || '', company || undefined);
    setOpen(false);
  };

  const handlePopularCompanyClick = (company: PopularCompany) => {
    const companyData: Company = {
      symbol: company.symbol,
      nameKr: company.nameKr,
      nameEn: company.nameEn,
      sector: company.sector,
      logoPath: company.logoPath,
      marketCapDisplay: company.marketCap
    };
    handleCompanySelect(companyData);
  };

  const handleCategoryClick = (categoryId: string) => {
    setSelectedCategory(selectedCategory === categoryId ? '' : categoryId);
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

  const renderCustomDropdown = () => (
    <Paper sx={{ mt: 1, maxHeight: 400, overflow: 'hidden' }}>
      {/* 카테고리 필터 */}
      <Box p={2}>
        <Typography variant="caption" color="text.secondary" gutterBottom>
          카테고리
        </Typography>
        <Box display="flex" flexWrap="wrap" gap={1} mt={1}>
          {categories.map((category) => (
            <Chip
              key={category.id}
              label={`${category.name} (${category.count})`}
              size="small"
              variant={selectedCategory === category.id ? "filled" : "outlined"}
              onClick={() => handleCategoryClick(category.id)}
              data-testid={`category-filter-${category.id}`}
              sx={{
                borderColor: selectedCategory === category.id ? 'primary.main' : 'divider'
              }}
            />
          ))}
        </Box>
      </Box>

      <Divider />

      {/* 검색어가 없을 때 인기 종목 표시 */}
      {searchQuery.trim() === '' && selectedCategory === '' && (
        <Box p={2}>
          <Typography variant="caption" color="text.secondary" gutterBottom>
            인기 종목
          </Typography>
          <Grid container spacing={1} mt={1}>
            {popularCompanies.map((company) => (
              <Grid item xs={6} key={company.symbol}>
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
                        {company.marketCap}
                      </Typography>
                    </Box>
                  </Box>
                </Box>
              </Grid>
            ))}
          </Grid>
        </Box>
      )}

      {/* 검색 결과 */}
      {(searchQuery.trim() !== '' || selectedCategory !== '') && (
        <Box>
          {loading ? (
            <Box display="flex" justifyContent="center" p={2}>
              <CircularProgress size={24} />
            </Box>
          ) : (
            <Box sx={{ maxHeight: 240, overflow: 'auto' }}>
              {options.map((option) => renderOption({ onClick: () => handleCompanySelect(option) }, option))}
              {options.length === 0 && (
                <Box p={2} textAlign="center">
                  <Typography variant="body2" color="text.secondary">
                    검색 결과가 없습니다
                  </Typography>
                </Box>
              )}
            </Box>
          )}
        </Box>
      )}
    </Paper>
  );

  return (
    <Box>
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
            InputProps={{
              ...params.InputProps,
              endAdornment: (
                <>
                  {loading && <CircularProgress color="inherit" size={20} />}
                  {params.InputProps.endAdornment}
                </>
              ),
            }}
          />
        )}
        PaperComponent={() => (
          <div data-testid="company-autocomplete-dropdown">
            {renderCustomDropdown()}
          </div>
        )}
        sx={{ mb: 2 }}
      />
    </Box>
  );
}