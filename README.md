# rao-training

Change the code starting at this line:
~~~java
Crac crac = null;
~~~

You can use methods 'drawNetwork' if you want to draw the network.

## Preventive RAO
1. Create a CRAC with a preventive, an outage, and a curative instant
2. Create a preventive CNEC on line "NNL2AA1  BBE3AA1  1", limit it to -500/+500 MW
3. Run RAO, what happens?
4. Create a network action (PRA) that closes "NNL2AA1  BBE3AA1  1"
5. Run RAO, what happens?

## N-1 curative RAO
1. Create a contingency "co1" on the loss of "FFR2AA1  NNL3AA1  1"
2. Create a curative CNEC on line "NNL2AA1  BBE3AA1  1", after co1, limit it to -500/+500 MW
3. Run RAO, what happens?
4. Add a CRA to close "NNL2AA1  BBE3AA1  3"
5. Run RAO, what happens?

## N-2 curative RAO
1. Create a contingency "co2" on the loss of "FFR2AA1  NNL3AA1  1" & "FFR1AA1  FFR3AA1  1"
2. Run RAO, what happens?
3. Add a CRA to close "NNL2AA1  BBE3AA1  3" & reduce injection on "NNL1AA1 _generator" from 1500 to 1200, and increase generation on "FFR1AA1 _generator" from 0 to 300
4. Run RAO, what happens?

## Outage CNEC
1. Re-create preventive & N-1 case
2. Replace curative CNEC with outage CNEC
3. Make the CRA to close "NNL2AA1  BBE3AA1  3" a PRA
4. Run RAO, what happens?

## Unsecure network
1. Re-create N-2 case
2. Remove one of the two network actions
3. Run RAO, what happens?