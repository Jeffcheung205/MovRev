# import requests
import json
import pandas as pd
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from flask import Flask, request, jsonify
# from pyngrok import ngrok

# !ngrok authtoken 2q12vrPVIET4xU2m9e5C7W5VNQC_gXZS6vSRLKL9KYcHkDsP
# public_url = ngrok.connect(5000)
# print(f"Public URL: {public_url}")

app = Flask(__name__)

def fetch_movie_data():
    php_url = "http://10.0.2.2/recommend.php?data=all"
    try:
        response = requests.get(php_url)
        if response.status_code == 200:
            data = response.json()
            if data['status'] == 'success':
                return data['movies']
            else:
                print(f"错误: {data['message']}")
        else:
            print(f"HTTP 错误: {response.status_code}")
    except Exception as e:
        print(f"请求错误: {e}")
    return []

def recommend_movies(data):
    fname = data['fname']
    movies = data['all_movies']
    if not movies:
        return {"status": "error", "message": "无法获取电影数据"}

    df = pd.DataFrame(movies)
    if fname not in df['fname'].values:
        return {"status": "error", "message": f"电影 {fname} 未找到"}

    vectorizer = CountVectorizer()
    tag_vectors = vectorizer.fit_transform(df['tname'].fillna(''))
    target_idx = df[df['fname'] == fname].index[0]
    target_vector = tag_vectors[target_idx]
    similarities = cosine_similarity(target_vector, tag_vectors).flatten()
    similar_indices = similarities.argsort()[-11:-1][::-1]
    recommended_fids = df.iloc[similar_indices]['fid'].tolist()

    return {
        "status": "success",
        "recommended_fids": recommended_fids
    }

@app.route('/recommend', methods=['POST'])
def handle_recommend():
    data = request.get_json()
    result = recommend_movies(data)
    return jsonify(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

    