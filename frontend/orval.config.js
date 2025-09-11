module.exports = {
  stockquest: {
    input: {
      target: '../docs/openapi.yml',
    },
    output: {
      mode: 'tags-split',
      target: 'src/shared/api/generated',
      schemas: 'src/shared/api/generated/model',
      client: 'react-query',
      httpClient: 'axios',
      mock: {
        type: 'msw',
        delay: 1000,
      },
      override: {
        mutator: {
          path: 'src/shared/api/api-client.ts',
          name: 'apiClient',
        },
        operations: {
          // React Query 훅 이름 커스터마이징
          'signup': {
            operationName: 'signup',
          },
          'login': {
            operationName: 'login',
          },
        },
      },
    },
    hooks: {
      afterAllFilesWrite: 'prettier --write',
    },
  },
};