# Gossip

Gossip is a messaging platform built in Java with JavaFX, MaterialFX, Java sockets, and MongoDB. It provides an elegant user interface for chatting and includes a multithreaded server for handling authentication and data requests.

## Features

- **Server Module:** Multithreaded server for authentication and data management.
- **Client Module:** JavaFX interface for real-time chatting.
- **Java Socket Communication:** Enables communication between the server and clients.
- **MaterialFX Styling:** Utilizes MaterialFX for modern and appealing UI.
- **MongoDB Integration:** Stores user data and messages in a MongoDB database.

## Prerequisites

- Java 18
- MongoDB (version 4.7.2)

## Getting Started

1. Clone the repository:

    ```bash
    git clone https://github.com/eleusKUET/Gossip.git
    cd Gossip
    ```

2. Set up MongoDB:

    ```bash
    sudo service mongod start
    ```

3. Compile and run the server:

    ```bash
    cd server
    javac ServerMain.java
    java ServerMain
    ```

4. Compile and run the client:

    ```bash
    cd client
    javac ClientMain.java
    java ClientMain
    ```

## Configuration

- Server configuration options can be found in `server/config.properties`.
- Client configuration options can be found in `client/config.properties`.

## Usage

- Launch the client, where a login screen will appear.
- Enter your credentials or register if you are a new user.
- Start chatting with friends using the intuitive user interface.

## Contributing

If you'd like to contribute to this project, please follow the [contribution guidelines](CONTRIBUTING.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Thank you to the authors and contributors of libraries/frameworks used.

## Contact

- Maintainer: Eleus Hasan (GitHub: [eleusKUET](https://github.com/eleusKUET))

