module.exports = {
  stockquest: {
    input: {
      target: './openapi-spec-new.json',
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
        operations: false,
      },
    },
    hooks: {
      afterAllFilesWrite: 'prettier --write',
    },
  },
};