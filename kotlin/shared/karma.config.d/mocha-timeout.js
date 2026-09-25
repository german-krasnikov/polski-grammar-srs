// Cold JS browser initialization can exceed Mocha's two-second default.
config.set({
  client: {
    ...config.client,
    mocha: { timeout: 10000 },
  },
});
