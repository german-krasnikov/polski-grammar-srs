// Cold JS test fixtures initialize the exercise generator and initial training chain; measured
// 15–17 seconds in TrainingStoreTest under ChromeHeadless, while the existing 10-second cap fails.
config.set({
  client: {
    ...config.client,
    mocha: { timeout: 30000 },
  },
});
