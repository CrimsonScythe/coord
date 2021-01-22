from sklearn.ensemble import GradientBoostingRegressor
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
import sys
import pickle
#################

# X, y = make_regression(n_features=4, n_informative=2, random_state=0, shuffle=False)

def main(argv):
    
    model=argv[0].split("/")[-1].split(".")[0]
    dataPath = argv[1]
    testPath = argv[2]
    mode = argv[3]
    column = argv[4]
    id = argv[5]


    df = pd.read_csv(dataPath)

    df = df.select_dtypes(include=np.number)    
    df = df.drop(columns=["ID"])

    # print(df)

    df=df.fillna(df.mean())


    # X_train, X_test, y_train, y_test = train_test_split(df.drop(columns=[column]), df[column], test_size=0.33)
    X_train = df.drop(columns=[column])
    y_train = df[column]

    ##################

    regr = GradientBoostingRegressor(random_state=0, verbose=100)

    regr.fit(X_train, y_train)

    # with open("/usr/src/"+id+model+".pkl", "wb") as f:
    #     pickle.dump(regr, f)

    df = pd.read_csv(testPath)
    df = df.select_dtypes(include=np.number)
    df = df.drop(columns=["ID"])

    df = df.fillna(df.mean())

    X_test = df.drop(columns=[column])
    y_test = df[column]

    # with open("/usr/src/"+id+model+".pkl", "rb") as f:
    #     regtest = pickle.load(f)


    output = regr.predict(X_test)

    np.savetxt(id+model+".csv", output, delimiter=",")

    score = regr.score(X_test, y_test)

    print("done"+" "+str(score), flush=True)

if __name__ == "__main__":
    main(sys.argv)
else:
    main(sys.argv)



#########################################


# df = pd.read_csv("/home/kamal/Downloads/data/ks-projects.csv")

# df = df.select_dtypes(include=np.number)    
# df = df.drop(columns=["ID"])

# print(df)

# df=df.fillna(df.mean())

# column="pledged"

# X_train, X_test, y_train, y_test = train_test_split(df.drop(columns=[column]), df[column], test_size=0.33)


# ################

# regr = RandomForestRegressor(random_state=0, verbose=100)
                         
# regr.fit(X_train, y_train)

# # regr.predict(X_test)
