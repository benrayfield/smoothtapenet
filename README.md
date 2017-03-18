#smoothtapenet wave based neural turing machine with 1 tape per node and 4 edge types - move writeValue writeDecay stdDev - moves and bends like signal processing instead of quantized

Youtube channel: https://www.youtube.com/channel/UCzz4QpV748wTJRFXWzxlhig

Doubleclick the jar file to open a fullyConnected neuralnet of 8 nodes (tapes) with random edges. Hold mouse button to pull the tape's center toward mouse height with bellcurve density left/right. Mouse left/right slides the tape. The 2 lines show 1 stdDev.

stdDev, movement, writeValue, and writeDecay are all computed by neuralnet. Each node/tape has 1 value read near its center blurred around that stdDev.

If you draw random curves in some of the nodes, you will see it create nonlinear curves and movements back and forth. It normally stabilizes on a single direction or vibrating back and forth per node, since its a random neuralnet. It will look like a heartbeat sometimes. For longer turing completeness it has to be trained.

I'm looking for help designing a learning algorithm for time series. This is a new kind of AI thats well defined how it runs but not how to adjust the weights. This could be an AGI.
