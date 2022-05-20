package com.game.ks2mathgame.game;

import java.util.Random;

class Question {
    private static int optNum = 4;  //every question must have 4 options

    private Random random;
    int a, b, operator;
    private int ans;

    private int minValue = 0;  //related to difficulty of question
    //this value is added to random generated value so value becomes bigger and harder

    private final int randRange = 5;  //range in which the value should be generated, this works in harness with minvalue

    //options
    int[] options = new int[optNum];

    Question(char operator, int minValue){
        //which type of question to generate such as addition, subtraction, multiplication or division
        this.operator = operator;
        this.minValue = minValue;

        random = new Random();
        a = minValue + Math.abs(random.nextInt() % (randRange));  //this number always becomes bigger with the increasing level or difficulty, so this is related to difficulty
        b = Math.abs(random.nextInt() % (randRange+minValue));  //this number's range becomes bigger with the increasing level so this is related to more randomness

        /*
        the random generation for division question is different
        because there is possibility for undefined answer if 1/0 form
        and there is also a possibility of random generated number that
        it may not be divisible by the second number
        */
        if(operator == '/'){
            //we generate a random denominator
            b = 1 + b;  //removes the possibility of 1/0 form

            //now we generate a random multiplier which is used to generate nominator
            //so in this way nominator is always greater than denominator and is fully divisible
            a = b * a;  // makes the 'a' fully divisible by 'b' because 'a' is 'b' times of 'a'
        }
        calculateAns();
        setOptions();
    }

    public void calculateAns(){
        //calculate answer based on the operator
        switch (operator){
            case '*': ans = a * b;
            break;
            case '/': ans = a / b;
            break;
            case '+': ans = a + b;
            break;
            case '-': ans = a - b;
            break;
            default : ans = 0;
        }
    }

    public void setOptions(){
        //we create all wrong answers first
        for(int i = 0; i < optNum; i++){
            /*
            Create a random number then either subtract or add the original answer from it (which is also random)
            so that all wrong answers are near to the answer, which will be more difficult
            For example if answer is 50, then it should be better to have other options as 45, 59, 30 instead of  having 12, 5, 99 etc
            */
            options[i] = minValue/2 + (random.nextInt() % randRange)  + (ans * plus_or_minus());  //create a wrong random ans
            /*
            options can contain negative sign only if there is minus operator
            so if this question is not a subtraction question
            we make the option absolute so that it does not contains negative sign at all
            */
            if(operator != '-'){
                options[i] = Math.abs(options[i]);
            }
        }
        //Now we place correct ans at random position in the array
        int randPos = Math.abs(random.nextInt() % optNum);  //index can never be negative, so apply absolute operation
        options[randPos] = ans;  //so there is only random place in array at which we have correct answer
    }

    public boolean checkAns(int response){
        return response == ans;
    }

    private int plus_or_minus(){
        if(random.nextInt()%2 == 0)  //if even return +1
            return +1;
        else
            return -1;  // if odd return -1
    }
}
