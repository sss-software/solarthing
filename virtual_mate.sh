# A 'u' character means that the program shouldn't check that character.
# usually, in an actual mate packet, that unused character will always be a 0
sleep 4
while true
do
    printf "\n1,10,10,10,100,100,10,03,000,00,282,000,000,999\r"
    printf "\n2,10,10,10,100,100,10,03,000,00,282,000,000,999\r"
    printf "\nD,uu,10,99,999,888,u7,09,000,04,282,000,000,999\r"
    sleep 3
done

