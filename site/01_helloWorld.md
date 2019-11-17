# Hello World
Just a playground for some scalajs graphic stuff. This has all been done many times before, but it's fun.

# Messaging
I'd like to replicate the RaftIO site where you can visualize messages sent in a system.

We need to be able to position time at any point, control the speed, and allow it to 'tail'
the latest values.

## Position at a time:

This actually looks the same as tailing - we just obviously can't go 2x speed at "latest"
when we're up-to-date.

So, the flow is:

user positions control at a time (including latest)
  +
  |
  +--- messages query --> message server  
                                +
                                |
        -------- messages ------+ 
                         