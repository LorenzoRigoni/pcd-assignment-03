package main

import (
	"fmt"
	"math/rand"
	"time"
)

var moves = []string{"rock", "paper", "scissors"}

// if beats[move1] == move2 => player1 wins
// if move1 == beats[move2] => player2 wins
var beats = map[string]string{
	"rock":     "scissors",
	"paper":    "rock",
	"scissors": "paper",
}

/*
	Type for move message
*/
type MoveRequest struct {
	move chan string
}

/*
	Type for result
*/
type Result struct {
	win  bool
	defeat bool
}

/*
	This function represents the player. He sends a random move to the referee and print the result received (win, defeat or draw).
*/
func player(name string, requestChan chan MoveRequest, resultChan chan Result) {
	scoreWins := 0
	scoreLoss := 0

	for {
		move := moves[rand.Intn(3)]
		moveReply := make(chan string, 1) //Create a buffered chan to avoid deadlocks
		moveReply <- move
		requestChan <- MoveRequest{move: moveReply}

		result := <-resultChan
		if result.win {
			scoreWins++
			fmt.Printf("[%s] I won! Score: %dW - %dL\n", name, scoreWins, scoreLoss)
		} else if result.defeat {
			scoreLoss++
			fmt.Printf("[%s] I lost! Score: %dW - %dL\n", name, scoreWins, scoreLoss)
		} else {
			fmt.Printf("[%s] Draw! Score: %dW - %dL\n", name, scoreWins, scoreLoss)
		}

		time.Sleep(1 * time.Second)
	}
}

/*
	This function represents the referee. He receives the two moves from the players, calc the result and send it to each player.
*/
func referee(p1ReqChan, p2ReqChan chan MoveRequest, p1ResChan, p2ResChan chan Result) {
	for {
		req1 := <-p1ReqChan
		req2 := <-p2ReqChan

		move1 := <-req1.move
		move2 := <-req2.move

		fmt.Printf("[Referee] Player1: %s | Player2: %s\n", move1, move2)

		var p1Res, p2Res Result

		if move1 == move2 {
			p1Res, p2Res = Result{}, Result{}
		} else if beats[move1] == move2 {
			p1Res = Result{win: true}
			p2Res = Result{defeat: true}
		} else {
			p1Res = Result{defeat: true}
			p2Res = Result{win: true}
		}

		p1ResChan <- p1Res
		p2ResChan <- p2Res
	}
}

func main() {
	p1ReqChan := make(chan MoveRequest)
	p2ReqChan := make(chan MoveRequest)
	p1ResChan := make(chan Result)
	p2ResChan := make(chan Result)

	go player("Player1", p1ReqChan, p1ResChan)
	go player("Player2", p2ReqChan, p2ResChan)
	go referee(p1ReqChan, p2ReqChan, p1ResChan, p2ResChan)

	select {}
}