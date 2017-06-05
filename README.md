# Communicate

## About 

Communicate is a spiritual successor to lopex/multibot and OlegYch/multibot

This family of libraries allows you to
create a bot that enables code interpretation.

Currently the difficulty arises when a fork of multibot fixes a bug,
It has to be propagated out to the other repos manually, often times with individual fixes.
It would be nice if we had a sane way of versioning these different things.

Also given that now both IRC and Gitter are supported in multibot (DavidDudson/multibot), 
and not everyone wants both, nor should they be required to. 
Nor should every bot manage interpreters for 20 different programming languages.

This is where the modularity comes in.

Often using the bot is useful for educational purposes. 
Or finding if a bug is reproducible.

## Goals

- Support multiple languages
- Support multiple interpreters for a single language
  - Different language platforms
  - Different language versions
  - Different library versions
- Support multiple frontends (IRC/Gitter/Slack)
- Be extremely modular

## Planned api support

- Chatrooms
 - Gitter
 - IRC
- Forums and Issue trackers
 - Github

## Planned language support

- Scala
  - JVM
  - JS
  - Native
  - Possibly a scastie backend 
- Haskell
- Idris

## Navigating the repo

- ./interpreters/$language holds the actual code interpreters
- ./api/$api holds the code to interface with specific api