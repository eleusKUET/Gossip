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
    cd GossipServer
    javac Main.java
    java Main
    ```

4. Compile and run the client:

    ```bash
    cd GossipUser
    javac Main.java
    java Main
    ```
## Video Demo

Check out the [Gossip Demo Video](https://github.com/eleusKUET/Gossip/blob/main/GossipVideo.mp4) for a visual walkthrough of the application.

## Usage

- Launch the client, where a login screen will appear.
- Enter your credentials or register if you are a new user.
- Start chatting with friends using the intuitive user interface.

## Contributing

If you'd like to contribute to this project, please email me.

## License

This project is open for development.

## Acknowledgments

- Thank you to the authors and contributors of libraries/frameworks used.

## Contact

- Maintainer: Eleus Ahammad (GitHub: [eleusKUET](https://github.com/eleusKUET))

