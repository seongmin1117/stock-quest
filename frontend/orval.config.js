module.exports = {
  stockquest: {
    input: {
      target: './openapi-spec.yaml',
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
          // Only exclude specific problematic actuator cache endpoints
          excludeFromGeneration: (operationObject, route) => {
            // Only exclude actuator cache endpoints that cause TypeScript generic errors
            return route.includes('actuator/caches/');
          }
        },
      },
    },
    hooks: {
      afterAllFilesWrite: 'prettier --write',
    },
  },
};