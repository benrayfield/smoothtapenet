#smoothtapenet wave based neural turing machine with 1 tape per node and 4 edge types - move writeValue writeDecay stdDev - moves and bends like signal processing instead of quantized

The prototype is a fullyConnected (times 4 edge types) neuralnet of 8 tapes with random edges. The edges dont change in this version.

In each tape there are 4 vectors, sets of edges from all the tapes (variable size bellcurve view of its center). Each of these is weightedSummed then sigmoid then scaled into a range. MOVE controls direction and speed. WRITEVALUE is the target value to decay the center toward. WRITEDECAY is how much to decay toward that target. STDDEV defines a bellcurve to read and write at. Decay is actually bellcurve height times WRITEDECAY.

Youtube channel: https://www.youtube.com/channel/UCzz4QpV748wTJRFXWzxlhig

Hold mouse button to pull the tape's center toward mouse height with bellcurve density left/right. Mouse left/right slides a tape. The 2 lines show 1 stdDev. If you draw random curves in some of the nodes, you will see it create nonlinear curves and movements back and forth. It normally stabilizes on a single direction or vibrating back and forth per node, since its a random neuralnet. It will look like a heartbeat sometimes. For longer turing completeness it has to be trained.

I'm looking for help designing a learning algorithm for time series. This is a new kind of AI thats well defined how it runs but not how to adjust the weights. This could be an AGI.
