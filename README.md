# SimpleBitcoinNode

`SimpleBitcoinNode` is a simplified implementation of the Bitcoin protocol according to the original [2008 whitepaper published by Satoshi Nakamoto](https://bitcoin.org/bitcoin.pdf), 
which serves as an educational tool about blockchain technology and as a project in university course Object-Oriented Programming. In addition to the simplified protocol the project also provides 
a wallet implementation to use for managing tokens. Wallet software repository can be found [here](https://github.com/inugami-dev64/SimpleBitcoinWallet).

## Getting started

The easiest way to build and package the project is by using the included maven wrapper. Make sure that JDK which supports Java 17 is installed,
in which case you can run following commands.  

Linux/MacOS:  
```sh
$ ./mvnw package
```

Windows:  
```cmd
> .\mvnw.cmd package
```

After which simply run the built jar file.

## Future roadmap and current project status

Please read this [document](docs/ROADMAP.md) for more information.

## Contributors 

* Karl-Mihkel Ott ([inugami-dev64](https://github.com/inugami-dev64))
* Dmitri Matetski ([TheGreyCore](https://github.com/TheGreyCore))
* Martin Toomiste ([toomistm](https://github.com/toomistm), *his contributions are mostly to [SimpleBitcoinWallet](https://github.com/inugami-dev64/SimpleBitcoinWallet) repository*)
