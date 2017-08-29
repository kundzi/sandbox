import Data.Function
import Data.Tuple


-- Problem 1

-- fibonacci n
fibonacci :: Integer -> Integer
fibonacci n
        | n < 0  	= (-1) ^ ((-n) + 1) * fibonacci (-n)
        | n == 0 	= 0
        | n > 0  	= helper n 1 0 1
-- helper n i a b 
helper :: Integer -> Integer -> Integer -> Integer -> Integer
helper n i a b
		| i == n	= b		
		| i < n  	= helper n (i + 1) b (a + b)

-- let EXPR in 
roots a b c =
	let d = sqrt (b ^ 2 - 4 * a * c) in
		(
			(-b - d) / (2 * a)
			,
			(-b + d) / (2 * a)
		)

-- complex let
roots' a b c =
	let
		x1 = (-b - d) / (2 * a)
		x2 = (-b + d) / (2 * a)
		d = sqrt $ b ^ 2 - 4 * a * c
	in (x1, x2)

-- complex let
roots'' a b c = (x1, x2) where
	x1 = (-b - d) / (2 * a)
	x2 = (-b + d) / (2 * a)
	d = sqrt $ b ^ 2 - 4 * a * c

rootsDiff a b c = let
	(x1, x2) = roots a b c
	in x2 - x1

-- Problem 2
-- a0=1; a1=2; a2=3; ak+3 = ak+2 + ak+1 − 2ak.
--					 ak   = ak-1 + ak-2 - 2ak-3
seqA :: Integer -> Integer
seqA n
	|  n == 0 = 1
	|  n == 1 = 2
	|  n > 1 = let
	     -- helper n i k k-1 k-2
			helper n i a b c
				| i == n    = a
				| otherwise = helper n (i + 1) (a + b - 2 * c) a b 
	   in helper n 2 3 2 1
	| otherwise = error "WHY SO NEGATIVE?"

-- Problem 3
sum'n'count :: Integer -> (Integer, Integer)
sum'n'count 0 = (0,1)
sum'n'count n
		| n < 0 = sum'n'count (-n)
		| n > 0 = (sum n, count n) where
			sum 0   = 0
			sum n   = (mod n 10) + sum (div n 10)
			count 0 = 0
			count n = 1 + count (div n 10)

-- Problem 4
integration :: (Double -> Double) -> Double -> Double -> Double
integration f a b = (step / 2) * ( f a + f b + 2 * traps f 0 step (a + step) 1) where 
	step = (b - a) / n
	n    = 1000
	traps f acc step current iter
		| iter == n = acc
		| otherwise = traps f (acc + f current) step (current + step) (iter + 1)


-- Problem 5
getSecondFrom :: a -> b -> c -> b
getSecondFrom a b c = b

-- monomorphism
mono :: Char -> Char
mono x = x

apply2 f x = f (f x)

-- Problem 6
-- multSecond = g `on` h
-- g = (*)
-- h = snd

-- paramParam = g' `on` h'
-- g' = (+)
-- h'= \x -> fst $ fst x 

-- lambda
lenVec = \x -> \y -> sqrt $ x^2 + y^2
-- lenVec = \x y -> sqrt $ x^2 + y^2

-- Problem 7
on3 :: (b -> b -> b -> c) -> (a -> b) -> a -> a -> a -> c
on3 op f x y z = op (f x) (f y) (f z)

-- Composition, аналогичен (.)
compose f g = \x -> f (g x)

-- Эквивалентные выражения:
-- sumFstFst = (+) on (\pp -> fst $ fst pp)
-- sumFstFst = (+) on (fst . fst)

-- Problem 8
doItYourself = f . g . h
f = logBase 2
g = \x -> x ^ 3
h = max 42

-- Problem 9
class Printable a where
	toString :: a -> [Char]
instance Printable Bool where
	toString True = "true"
	toString False = "false"
instance Printable () where
	toString () =  "unit type"

-- Problem 10
instance (Printable a, Printable b) => Printable (a,b) where
	toString (x,y) = "(" ++ toString x ++ "," ++ toString y ++ ")"


-- show/read/reads
-- read "5" :: Int
-- read "5" :: Double
-- reads "5 rings" :: [(Int, String)]
 
 -- Problem 11
a = 127.2
b = 24.1
c = 20.1
d = 2
ip = show a ++ show b ++ show c ++ show d

-- Some standard class types
-- Enum, Bounded

-- Problem 12
class (Enum a, Bounded a, Eq a) => SafeEnum a where
  ssucc :: a -> a
  ssucc x
  	| x == maxBound		= minBound
  	| otherwise         = succ x

  spred :: a -> a
  spred x
  	| x == minBound 	= maxBound
  	| otherwise 		= pred x 
instance SafeEnum Bool

-- Problem 13
avg :: Int -> Int -> Int -> Double
avg a b c = fromIntegral a/3 + fromIntegral b/3 + fromIntegral c/3
