import pandas as pd
import xlrd
df = pd.read_excel('train.xls', sheet_name='train')

if(df.iloc[6]['toxic'] == 0):
    print(df.iloc[6])



