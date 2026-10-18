// ChromeHeadless on the audited host takes up to 4.97s for a sealed E2/E3 probe.
// This limit applies only to the isolated NJ05 read-only test module.
config.set({
  client: {
    mocha: { timeout: 10000 }
  }
});
