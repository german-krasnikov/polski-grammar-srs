// Browser compilation/initialization can exceed Mocha's 2-second default on cold runs.
config.set({
  client: {
    ...config.client,
    mocha: { timeout: 10000 },
  },
});
