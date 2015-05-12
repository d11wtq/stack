# Stack

Deploy AWS CloudFormation stacks with ease.

## Overview

Stack is a command line tool that wraps the AWS API to facilitate the
deployment of CloudFormation stacks. It has some smart features, such as:

  * Lazy stack creation on first deploy
  * Parameter passing in a config file style
  * Parameter overrides on the command line
  * Stack event polling
  * Client-side signalling based on ELB status

## Usage

Stack is sub-command driven, like git. Available commands are:

  * help
  * deploy
  * delete
  * events
  * signal

### help

    Usage: stack help [command] [opts...]

      -h, --help  Print usage info


Without a `[command]`, provides full program usage. With a `[command]`, prints
usage information for the given command.

    $ stack help deploy

### deploy

    Usage: stack deploy <stack-name> <template> [opts...] [key=value...]

      -h, --help            Print usage info
      -p, --params FILE     Read parameters from FILE
      -s, --signal ELB:ASG  Poll ELB for healthy instances and signal ASG

Lazily creates or updates `<stack-name>` with contents of the JSON file
`<template>`.

If `--params` is provided, the contents of the JSON `FILE` will be expanded and
applied to the template.

If any `key=value` arguments follow the command, they will be used as params to
the template and have a higher precedence than those provided by `--params`.

Stack events are output until the stack update completes.

    $ stack deploy                \
    >   blog-stack                \
    >   docker-stack.json         \
    >   --params blog-params.json \
    >   hostPort=8080

### delete

    Usage: stack delete <stack-name> [opts...]

      -h, --help  Print usage info

Deletes `<stack-name>`.

Stack events are output until the delete completes.

    $ stack delete blog-stack

### events

    Usage: stack events <stack-name> [opts...]

      -h, --help    Print usage info
      -f, --follow  Poll for new events
      -u, --update  Used with --follow, stop polling after stack update

Outputs stack events for `<stack-name>` in chronological order.

If `--follow` is specified, keeps checking for new stack events and never
exits.

If `<stack-name>` does not exist, outputs a non-fatal warning.

    $ stack events blog-stack --follow

### signal

    Usage: stack signal <stack-name> <elb>:<asg> [opts...]

      -h, --help    Print usage info
      -u, --update  Stop signalling after stack update

Polls the logical resource `<elb>` (which must be of type
`AWS::ElasticLoadBalancing::LoadBalancer`) looking for healthy EC2 instances
and sends signals to the logical resource `<asg>` (which must be of type
`AWS::EC2::AutoScalingGroup`) for each healthy instance found.

Outputs instance states as changes occur.

If `<elb>` or `<asg>` do not exist, outputs a non-fatal warning.

This command never exits unless `--update` is given.

    $ stack signal blog-stack loadBalancer:autoScalingGroup

## License

Copyright © 2015 Chris Corbyn. See the LICENSE file for details.
